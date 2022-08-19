package com.wsdy.saasops.modules.member.service;

import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.modules.transfer.dto.DepotFailDtosDto;
import com.wsdy.saasops.api.modules.transfer.service.TransferService;
import com.wsdy.saasops.api.modules.user.dto.UserBalanceResponseDto;
import com.wsdy.saasops.api.modules.user.service.DepotWalletService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BigDecimalMath;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.member.dto.AuditBonusDto;
import com.wsdy.saasops.modules.member.dto.DepotFailDto;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MbrDepotAsyncWalletService {

    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private TransferService transferService;

    @Async
    public CompletableFuture<UserBalanceResponseDto> getAsyncBalance(Integer depotId, Integer userId, String siteCode) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        TGmApi gmApi = gmApiService.queryApiObject(depotId, siteCode);
        UserBalanceResponseDto balanceDto = null;
        try {
            balanceDto = depotWalletService.queryDepotBalance(userId, gmApi);
        } catch (Exception ex) {
        }
        if (balanceDto == null) {
            balanceDto = new UserBalanceResponseDto();
            balanceDto.setBalance(new BigDecimal("0"));
        }
        balanceDto.setDepotId(depotId);
        return CompletableFuture.completedFuture(balanceDto);
    }

    /**
     * 废弃：旧的转账接口 --> 不支持apigateway回收余额的平台可以走这个逻辑
     * 批量回收平台余额： 平台->中心钱包
     * @param depotFailDtosDto  depotFailDtosDto.getDepotWallets()  要回收的平台钱包
     * @return
     */
    public List<DepotFailDto> getAsyncRecoverBalance(DepotFailDtosDto depotFailDtosDto) {
        // 并发回收余额，并汇总结果
        List<CompletableFuture<DepotFailDto>> depotFindingFutureList =
                depotFailDtosDto.getDepotWallets().stream().map(
                        e1 ->  depotTransfeFuture(e1.getDepotId(), e1, depotFailDtosDto.getSiteCode(),
                                depotFailDtosDto.getIp(),depotFailDtosDto.getTransferSource(), Boolean.TRUE)
                ).collect(Collectors.toList());

        CompletableFuture<Void> allFutures =
                CompletableFuture
                        .allOf(depotFindingFutureList.toArray(
                                new CompletableFuture[depotFindingFutureList.size()]));

        CompletableFuture<List<DepotFailDto>> depotFailResults = allFutures.thenApply(v -> {
            return depotFindingFutureList.stream().map(
                    depotFailDtoCompletableFuture -> depotFailDtoCompletableFuture.join())
                    .collect(Collectors.toList());
        });
        try {
            return depotFailResults.get();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *  并发回收平台余额
     * @param depotId       平台id
     * @param wallet        平台钱包对象
     * @param siteCode      站点siteCode
     * @param ip            ip
     * @param transferSource    客户端
     * @param isTransferBouns   此处为true  转出是否判断优惠转出 true判断 false不用
     * @return
     */
    CompletableFuture<DepotFailDto> depotTransfeFuture(Integer depotId, MbrDepotWallet wallet, String siteCode, String ip, Byte transferSource, Boolean isTransferBouns) {
        return CompletableFuture.supplyAsync(() -> {
            DepotFailDto depotFailDto = new DepotFailDto();
            depotFailDto.setDepotId(depotId);
            depotFailDto.setFailError(Boolean.TRUE);
            try {
                ThreadLocalCache.setSiteCodeAsny(siteCode);
                TGmApi gmApi = gmApiService.queryApiObject(depotId, siteCode);
                // 1. 先查询余额平台余额
                UserBalanceResponseDto balanceDto = depotWalletService.queryDepotBalance(wallet.getAccountId(), gmApi);
                balanceDto.setBalance(BigDecimalMath.formatDownRounding(balanceDto.getBalance()));
                // 2. 平台有余额则回收余额：转账 平台->中心钱包
                if (balanceDto.getBalance().doubleValue() > 0) {
                    // 创建BillRequestDto对象
                    BillRequestDto requestDto = getBillRequestDto(balanceDto.getBalance(), depotId, wallet, ip);
                    requestDto.setTransferSource(transferSource);

                    // isTransferBouns 恒为true,此处if不执行（转出是否判断优惠转出 true判断 false不用）
                    if (Boolean.FALSE.equals(isTransferBouns)) {
                        requestDto.setIsTransferBouns(isTransferBouns);
                    }
                    // 转账：旧的转账逻辑，废弃
                    AuditBonusDto auditBonusDto = transferService.TransferOut(requestDto, siteCode);
                    if (Boolean.FALSE.equals(auditBonusDto.getIsSucceed()) || Boolean.FALSE.equals(auditBonusDto.getIsFraud())) {
                        depotFailDto.setIsSign(Constants.EVNumber.one);
                        depotFailDto.setFailError(false);
                    }
                }
            } catch (Exception e) {
                depotFailDto.setIsSign(Constants.EVNumber.zero);
                depotFailDto.setFailError(false);
            }
            return depotFailDto;
        });
    }

    public BillRequestDto getBillRequestDto(BigDecimal balance, Integer depotId, MbrDepotWallet mbrWallet, String ip) {
        BillRequestDto requestDto = new BillRequestDto();
        requestDto.setAmount(balance);
        requestDto.setDepotId(depotId);
        requestDto.setAccountId(mbrWallet.getAccountId());
        requestDto.setLoginName(mbrWallet.getLoginName());
        requestDto.setIp(ip);
        return requestDto;
    }
}
