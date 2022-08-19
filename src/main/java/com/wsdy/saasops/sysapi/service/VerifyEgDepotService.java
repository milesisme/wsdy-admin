package com.wsdy.saasops.sysapi.service;

import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.modules.transfer.dto.DepotFailDtosDto;
import com.wsdy.saasops.api.modules.transferNew.service.DepotService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.fund.service.FundReportService;
import com.wsdy.saasops.modules.member.dao.MbrBillManageMapper;
import com.wsdy.saasops.modules.member.dao.MbrDepotWalletMapper;
import com.wsdy.saasops.modules.member.dto.AuditBonusDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.sysapi.dto.LoginDto;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class VerifyEgDepotService {

    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private DepotService depotService;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private FundReportService fundReportService;
    @Autowired
    private MbrBillManageMapper mbrBillManageMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private MbrDepotWalletMapper depotWalletMapper;
    @Autowired
    private TGmDepotMapper gmDepotMapper;

    public void transfermz(HttpServletRequest request, @ModelAttribute LoginDto loginDto, String siteCode, MbrAccount mbrAccount) {
        if (mbrAccount.getAvailable() == MbrAccount.Status.LOCKED) {
            throw new R200Exception("账户余额已冻结，不支持此操作，请联系管理员");
        }
        TGmApi gmApi = gmApiService.queryApiObject(27, siteCode);
        if (Objects.isNull(gmApi)) {
            throw new R200Exception("对不起，暂无此游戏线路");
        }
        MbrBillManage mbrBillManage = new MbrBillManage();
        mbrBillManage.setAccountId(mbrAccount.getId());
        mbrBillManage.setOpType(0);
        List<MbrBillManage> mbrBillManages = mbrBillManageMapper.select(mbrBillManage);
        if (Collections3.isNotEmpty(mbrBillManages)) {
            MbrBillManage mbrBillManage1 = mbrBillManages.get(mbrBillManages.size() - 1);
            MbrDepotWallet wallet1 = new MbrDepotWallet();
            wallet1.setDepotId(mbrBillManage1.getDepotId());
            wallet1.setLoginName(mbrBillManage1.getLoginName());
            MbrDepotWallet depotWallet = depotWalletMapper.selectOne(wallet1);
            DepotFailDtosDto depotFailDtosDto = new DepotFailDtosDto();
            depotFailDtosDto.setDepotWallets(Lists.newArrayList(depotWallet));
            depotFailDtosDto.setIp(CommonUtil.getIpAddress(request));
            depotFailDtosDto.setDev("PC");
            depotFailDtosDto.setTransferSource((byte) 0);
            depotFailDtosDto.setUserId(depotWallet.getAccountId());
            depotFailDtosDto.setLoginName(mbrAccount.getLoginName());
            depotFailDtosDto.setSiteCode(siteCode);
            mbrWalletService.getDepotFailDtos(depotFailDtosDto);
        }
        //TODO 查询主账户信息
        MbrWallet mbrWallet = fundReportService.queryAccountBalance(loginDto.getUserName());
        if (mbrWallet.getBalance().compareTo(BigDecimal.ONE) == Constants.EVNumber.one ||
                mbrWallet.getBalance().compareTo(BigDecimal.ONE) == Constants.EVNumber.zero) {
            BillRequestDto requestDto = new BillRequestDto();
            requestDto.setAccountId(mbrWallet.getId());

            TGmDepot depot = new TGmDepot();
            depot.setDepotCode(ApiConstants.DepotCode.EG);
            List<TGmDepot> list = gmDepotMapper.select(depot);
            if(Objects.nonNull(list) && list.size() > 0){
                requestDto.setDepotId(list.get(0).getId());
            }
            requestDto.setLoginName(loginDto.getUserName());
            BigDecimal bd = new BigDecimal(mbrWallet.getBalance().toString());
            requestDto.setAmount(bd.setScale(0, BigDecimal.ROUND_DOWN));
            MbrAccount account = new MbrAccount();
            account.setId(mbrAccount.getId());
            account.setLoginName(mbrAccount.getLoginName());
            AuditBonusDto auditBonusDto = auditAccountService.outAuditBonus(account, requestDto.getDepotId());
            if (Boolean.TRUE.equals(auditBonusDto.getIsSucceed()) && Boolean.TRUE.equals(auditBonusDto.getIsFraud())) {
                depotService.accountTransferOut(requestDto, siteCode);
            }
        }
    }
}
