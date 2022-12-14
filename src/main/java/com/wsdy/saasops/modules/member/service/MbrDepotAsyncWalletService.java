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
     * ??????????????????????????? --> ?????????apigateway??????????????????????????????????????????
     * ??????????????????????????? ??????->????????????
     * @param depotFailDtosDto  depotFailDtosDto.getDepotWallets()  ????????????????????????
     * @return
     */
    public List<DepotFailDto> getAsyncRecoverBalance(DepotFailDtosDto depotFailDtosDto) {
        // ????????????????????????????????????
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
     *  ????????????????????????
     * @param depotId       ??????id
     * @param wallet        ??????????????????
     * @param siteCode      ??????siteCode
     * @param ip            ip
     * @param transferSource    ?????????
     * @param isTransferBouns   ?????????true  ?????????????????????????????? true?????? false??????
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
                // 1. ???????????????????????????
                UserBalanceResponseDto balanceDto = depotWalletService.queryDepotBalance(wallet.getAccountId(), gmApi);
                balanceDto.setBalance(BigDecimalMath.formatDownRounding(balanceDto.getBalance()));
                // 2. ??????????????????????????????????????? ??????->????????????
                if (balanceDto.getBalance().doubleValue() > 0) {
                    // ??????BillRequestDto??????
                    BillRequestDto requestDto = getBillRequestDto(balanceDto.getBalance(), depotId, wallet, ip);
                    requestDto.setTransferSource(transferSource);

                    // isTransferBouns ??????true,??????if?????????????????????????????????????????? true?????? false?????????
                    if (Boolean.FALSE.equals(isTransferBouns)) {
                        requestDto.setIsTransferBouns(isTransferBouns);
                    }
                    // ????????????????????????????????????
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
