package com.wsdy.saasops.agapi.modules.service;

import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.pay.dto.DepositPostScript;
import com.wsdy.saasops.api.modules.pay.dto.PayParams;
import com.wsdy.saasops.api.modules.pay.dto.PayResponseDto;
import com.wsdy.saasops.api.modules.pay.service.PaymentService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.agent.dao.AgentDepositMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentDeposit;
import com.wsdy.saasops.modules.agent.mapper.DepositMapper;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.service.SaasopsPayService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrDepositCond;
import com.wsdy.saasops.modules.member.service.MbrDepositCondService;
import com.wsdy.saasops.modules.system.pay.dao.SetBacicFastPayMapper;
import com.wsdy.saasops.modules.system.pay.dao.SysDepositMapper;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicFastPay;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicOnlinepay;
import com.wsdy.saasops.modules.system.pay.entity.SysDeposit;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import static com.wsdy.saasops.common.constants.Constants.AGENT;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class AgentPayInfoService {

    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private AgentDepositMapper agentDepositMapper;
    @Autowired
    private MbrDepositCondService mbrDepositCondService;
    @Autowired
    private SysDepositMapper depositMapper;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private SetBacicFastPayMapper fastPayMapper;
    @Autowired
    private AgentQrCodePayService qrCodePayService;
    @Autowired
    private DepositMapper xmldepositMapper;
    @Autowired
    private SaasopsPayService saasopsPayService;
    @Autowired
    private PaymentService paymentService;


    // ????????????????????????
    public DepositPostScript getDepositPostScript(PayParams params) {
        log.info("????????????==loginName==" + params.getLoginName() + "==applyPay==" + "fee==" + params.getFee() + "==depositId==" + params.getDepositId());
        MbrAccount account = mbrAccountMapper.selectByPrimaryKey(params.getAccountId());
        // ??????????????????
        SysDeposit sysDeposit = getSysDeposit(params, account);
        // ??????????????????
        checkoutSysDeposit(sysDeposit, params);
        // ???????????????????????????
        MbrDepositCond mbrDepositCond = mbrDepositCondService.getMbrDeposit(params.getAccountId());
        // ??????deposit??????
        AgentDeposit deposit = saveFundDespoit(params, sysDeposit, mbrDepositCond, account);
        // ???????????????
        DepositPostScript script;
        // ???????????????????????????
        if (Objects.isNull(sysDeposit.getFastPayId())) {
            agentDepositMapper.insert(deposit);
            // ???????????????
            script = xmldepositMapper.findOfflineDepositInfo(deposit.getId());
            return script;
        }
        // ??????????????????
        script = fastPay(deposit, sysDeposit, params.getSiteCode());
        return script;
    }

    /**
     * ????????????????????????
     *
     * @param deposit    ????????????
     * @param sysDeposit ????????????-??????????????????
     * @param siteCode   siteCode
     * @return
     */
    private DepositPostScript fastPay(AgentDeposit deposit, SysDeposit sysDeposit, String siteCode) {
        // ??????????????????code
        String platFormCode = payMapper.findPayId(sysDeposit.getFastPayId());
        DepositPostScript resultObj = new DepositPostScript();
        // ????????????
        if (PayConstants.SAASOPS_PAY_CODE.equals(platFormCode)) {
            // ??????
            FundDeposit deposit1 = new FundDeposit();
            deposit1.setOrderNo(deposit.getOrderNo());
            deposit1.setDepositAmount(deposit.getDepositAmount());
            deposit1.setDepositUser(deposit.getDepositUser());
            deposit1.setDepositPostscript(deposit.getDepositPostscript());
            deposit1.setAccountId(deposit.getAccountId());
            deposit1.setLoginName(deposit.getLoginName());
            deposit1.setFundSource(deposit.getFundSource());
            deposit1.setIp(deposit.getIp());

            siteCode = siteCode + "_" + AGENT;
            resultObj = saasopsPayService.placeOrder(deposit1, sysDeposit, siteCode);
            // deposit??????
            deposit.setPayOrderNo(deposit.getOrderNo());     // ??????????????????
            agentDepositMapper.insert(deposit);
        }
        return resultObj;
    }

    private SysDeposit getSysDeposit(PayParams params, MbrAccount account) {
        SysDeposit deposit = depositMapper.selectByPrimaryKey(params.getDepositId());
        SysDeposit sysDeposit = null;
        // ???????????????
        if (isNull(deposit.getFastPayId())) {
            sysDeposit = payMapper.findDepositByGroupIdAndDepositId(account.getGroupId(), params.getDepositId());
        }
        // ???????????????????????????????????????bankCode
        if (nonNull(deposit.getFastPayId())) {
            sysDeposit = payMapper.findFastPayDepositByGroupId(account.getGroupId(), params.getDepositId());
        }
        return sysDeposit;
    }

    private AgentDeposit saveFundDespoit(PayParams params, SysDeposit sysDeposit, MbrDepositCond depositCond, MbrAccount account) {
        AgentAccount agentAccount = qrCodePayService.getAgyAccountByAccountId(account.getId());

        AgentDeposit deposit = new AgentDeposit();
        params.setOutTradeNo(new SnowFlake().nextId());
        deposit.setOrderNo(params.getOutTradeNo().toString());      // ?????????
        deposit.setCompanyPayId(params.getDepositId());             // ????????????ID
        deposit.setDepositUser(params.getUserName());               // ???????????????
        deposit.setAccountId(agentAccount.getId());                // ??????id
        deposit.setFundSource(params.getFundSource());              // ??????????????? 0 PC???3 H5
        deposit.setIp(params.getIp());                              // ????????????IP

        deposit.setDepositAmount(params.getFee());                  // ????????????
        Byte feeEnable = nonNull(depositCond) && nonNull(depositCond.getFeeEnable()) ? depositCond.getFeeEnable() : 0;  // ?????????????????????
        // ?????????????????????
        BigDecimal handlingCharge = getActualArrival(params.getFee(), sysDeposit, feeEnable);
        deposit.setHandlingCharge(handlingCharge);                  // ?????????
        deposit.setActualArrival(deposit.getDepositAmount().add(deposit.getHandlingCharge()));  // ????????????

        deposit.setCreateUser(agentAccount.getAgyAccount());              // ????????????
        deposit.setLoginName(agentAccount.getAgyAccount());               // ?????????

        deposit.setMark(FundDeposit.Mark.offlinePay);                           // ???????????????1 ????????????
        deposit.setStatus(FundDeposit.Status.apply);                            // ?????????2 ?????????
        deposit.setIsPayment(FundDeposit.PaymentStatus.unPay);                  // ???????????? false ?????????
        deposit.setHandingback(Constants.Available.disable);        // ?????????????????????(???1 ??????????????? ??????0 ??????????????????????????????)"
        deposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME)); // ????????????
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME)); // ????????????
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);   // ??????????????? CP ????????????
        deposit.setDepositPostscript(CommonUtil.genRandom(6, 6));   // ???????????????6?????????

        return deposit;
    }

    private void checkoutSysDeposit(SysDeposit sysDeposit, PayParams params) {
        if (Objects.isNull(sysDeposit)) {
            throw new R200Exception("?????????????????????????????????");
        }
        if (sysDeposit.getMinAmout() != null && sysDeposit.getMaxAmout() != null) {
            if (sysDeposit.getMinAmout().compareTo(params.getFee()) == 1) {
                throw new R200Exception("??????????????????????????????");
            }
            if (params.getFee().compareTo(sysDeposit.getMaxAmout()) == 1) {
                throw new R200Exception("??????????????????????????????");
            }
        }
        if (StringUtils.isNotBlank(sysDeposit.getFixedAmount())) {
            if (!sysDeposit.getFixedAmount().contains(String.valueOf(params.getFee().intValue()))) {
                throw new R200Exception("?????????????????????????????????");
            }
        }
        if (sysDeposit.getDepositAmount().compareTo(sysDeposit.getDayMaxAmout()) == 1) {
            throw new R200Exception("????????????????????????????????????????????????????????????????????????");
        }
    }

    public BigDecimal getActualArrival(BigDecimal fee, SysDeposit sysDeposit, Byte feeEnable) {
        if (feeEnable == Constants.EVNumber.zero) {
            return BigDecimal.ZERO;
        }
        BigDecimal bigDecimal = BigDecimal.ZERO;
        if (nonNull(sysDeposit.getFastPayId())) {
            SetBacicFastPay fastPay = fastPayMapper.selectByPrimaryKey(sysDeposit.getFastPayId());
            sysDeposit.setFeeWay(fastPay.getFeeWay());
            sysDeposit.setFeeFixed(fastPay.getFeeFixed());
            sysDeposit.setFeeTop(fastPay.getFeeTop());
            sysDeposit.setFeeScale(fastPay.getFeeScale());
        }
        if (sysDeposit.getFeeWay() == Constants.EVNumber.one) {
            bigDecimal = sysDeposit.getFeeFixed();
        }
        if (sysDeposit.getFeeWay() == Constants.EVNumber.zero) {
            bigDecimal = CommonUtil.adjustScale(sysDeposit.getFeeScale().divide(
                    new BigDecimal(Constants.ONE_HUNDRED)).multiply(fee));
            if (nonNull(sysDeposit.getFeeTop()) && bigDecimal.compareTo(sysDeposit.getFeeTop()) == 1) {
                bigDecimal = sysDeposit.getFeeTop();
            }
        }
        return bigDecimal;
    }

    public PayResponseDto optionPayment(PayParams params) {
        AgentDeposit deposit = optionSaveFundDespoit(params);
        SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(params.getOnlinePayId());
        if (PayConstants.SAASOPS_PAY_CODE.equals(onlinepay.getPlatfromCode())) {
            params.setAccountId(deposit.getAccountId());
            params.setSiteCode(params.getSiteCode() + "_" + AGENT);
            return paymentService.commonPay(params, onlinepay, null, deposit);
        }
        throw new R200Exception("???????????????");
    }

    public AgentDeposit optionSaveFundDespoit(PayParams params) {
        AgentAccount agentAccount = qrCodePayService.getAgyAccountByAccountId(params.getAccountId());

        AgentDeposit deposit = new AgentDeposit();
        deposit.setOrderNo(params.getOutTradeNo().toString());
        deposit.setMark(FundDeposit.Mark.onlinePay);
        deposit.setStatus(FundDeposit.Status.apply);
        deposit.setIsPayment(Boolean.FALSE);
        deposit.setOnlinePayId(params.getOnlinePayId());
        deposit.setDepositAmount(params.getFee());
        BigDecimal feeScale = BigDecimal.ZERO;
        deposit.setHandlingCharge(feeScale);
        deposit.setActualArrival(deposit.getDepositAmount().subtract(feeScale));
        deposit.setHandingback(Constants.Available.enable);
        deposit.setIp(params.getIp());
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_ONLINEDEPOSIT);
        params.setLoginName(agentAccount.getAgyAccount()); // ???????????????
        deposit.setDepositUser(agentAccount.getRealName());
        deposit.setCreateUser(agentAccount.getAgyAccount());
        deposit.setAccountId(agentAccount.getId());
        deposit.setCreateTime(DateUtil.format(new Date(), DateUtil.FORMAT_25_DATE_TIME));
        deposit.setModifyTime(DateUtil.format(new Date(), DateUtil.FORMAT_25_DATE_TIME));
        deposit.setFundSource(params.getFundSource());
        return deposit;
    }
}
