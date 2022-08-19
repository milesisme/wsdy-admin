package com.wsdy.saasops.api.modules.user.service;

import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AginService {

    @Autowired
    MbrWalletService mbrWalletService;

    /**
     * @param accountId
     * @param depotId
     * @param siteCode
     * @param loginName
     * @return
     */
    public MbrDepotWallet getDepotWallet(Integer accountId, Integer depotId, String siteCode, String loginName) {
        MbrDepotWallet wallet = new MbrDepotWallet();
        wallet.setAccountId(accountId);
        wallet.setDepotId(depotId);
        wallet = mbrWalletService.queryObjectCond(wallet, siteCode);
        if (wallet == null) {
            wallet = new MbrDepotWallet();
            wallet.setLoginName(loginName);
            wallet.setAccountId(accountId);
            wallet.setPwd(CommonUtil.genRandomNum(6, 8));
            wallet.setDepotId(depotId);
            wallet.setIsBuild(Boolean.FALSE);
        } else {
            wallet.setIsBuild(Boolean.TRUE);
        }
        return wallet;
    }
}
