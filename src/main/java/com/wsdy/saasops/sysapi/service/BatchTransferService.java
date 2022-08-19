package com.wsdy.saasops.sysapi.service;

import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.fund.service.FundReportService;
import com.wsdy.saasops.modules.member.dao.MbrDepotWalletMapper;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
public class BatchTransferService {

    @Autowired
    private MbrDepotWalletMapper depotWalletMapper;
    @Autowired
    private FundReportService fundReportService;

    public void accountTransfer(String siteCode, String depotCode) {
        MbrDepotWallet mbrDepotWallet = new MbrDepotWallet();
        mbrDepotWallet.setDepotName(depotCode);
        mbrDepotWallet.setIsTransfer((byte) 1);
        List<MbrDepotWallet> depotWallets = depotWalletMapper.select(mbrDepotWallet);
        for (MbrDepotWallet depotWallet : depotWallets) {
            batchAccountTransferExecutor(depotWallet, siteCode);
        }
    }

    @Transactional
    public void batchAccountTransferExecutor(MbrDepotWallet depotWallet, String siteCode) {
        CompletableFuture.runAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            BillRequestDto requestDto = new BillRequestDto();
            requestDto.setAccountId(depotWallet.getAccountId());
            requestDto.setLoginName(depotWallet.getLoginName());
            requestDto.setDepotId(depotWallet.getDepotId());
            requestDto.setOrderNo(String.valueOf(new SnowFlake().nextId()));
            requestDto.setOpType(1);
            requestDto.setTransferSource((byte) 2);
            log.info(depotWallet.getLoginName() + "开始任务转出到钱包");
            fundReportService.save(requestDto, siteCode);
        });
    }

}
