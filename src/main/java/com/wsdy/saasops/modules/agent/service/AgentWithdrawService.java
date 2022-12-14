package com.wsdy.saasops.modules.agent.service;

import com.google.gson.Gson;
import com.wsdy.saasops.api.modules.pay.dto.evellet.CommonEvelletResponse;
import com.wsdy.saasops.api.modules.pay.dto.evellet.EvelletPayTransferCallbackDto;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.*;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.config.MessagesConfig;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.agent.dao.*;
import com.wsdy.saasops.modules.agent.entity.*;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.modules.agent.mapper.WithdrawMapper;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.dao.FundMerchantPayMapper;
import com.wsdy.saasops.modules.fund.dao.TChannelPayMapper;
import com.wsdy.saasops.modules.fund.dto.*;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.fund.entity.TChannelPay;
import com.wsdy.saasops.modules.fund.service.*;
import com.wsdy.saasops.modules.member.entity.*;
import com.wsdy.saasops.modules.system.pay.service.CryptoCurrenciesService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class AgentWithdrawService extends BaseService<AgyWithdrawMapper, AgyWithdraw> {

    @Autowired
    private WithdrawMapper withdrawMapper;
    @Autowired
    private AgyBankCardMapper agyBankCardMapper;
    @Autowired
    private AgyWithdrawMapper agyWithdrawMapper;
    @Autowired
    private MessagesConfig messagesConfig;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private PanZiPayService panZiPayService;
    @Autowired
    private AgentMerchantDetailMapper merchantDetailMapper;
    @Autowired
    private FundMerchantPayMapper merchantPayMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private TChannelPayMapper channelPayMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OnePayService onePayService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private PaymentPayService paymentPayService;
    @Autowired
    private CryptoCurrenciesService cryptoCurrenciesService;
    @Autowired
    private AgyBillDetailMapper billDetailMapper;
    @Autowired
    private AgentWalletService walletService;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgentCryptoCurrenciesMapper cryptoCurrenciesMapper;


    public PageUtils queryAccListPage(AgyWithdraw accWithdraw, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<Integer> statuss = accWithdraw.getStatuss();
        //??????statuss????????????
        if (Collections3.isNotEmpty(statuss)) {
            int forFlag = statuss.size();
            for (int i = 0; i < statuss.size(); i++) {
                if (2 == statuss.get(i) || 4 == statuss.get(i)) {
                    statuss.add(4);
                    statuss.add(2);
                } else if (3 == statuss.get(i) || 5 == statuss.get(i)) {
                    statuss.add(3);
                    statuss.add(5);
                }
                if (i + 1 == forFlag) {
                    break;
                }
            }
        }
        List<AgyWithdraw> list = withdrawMapper.findAccWithdrawList(accWithdraw);
        return BeanUtil.toPagedResult(list);
    }

    public AgyWithdraw accSumStatistics(AgyWithdraw accWithdraw) {
        return withdrawMapper.findAccWithdrawListSum(accWithdraw);
    }

    public Double accSumDrawingAmount(String loginSysUserName) {
        AgyWithdraw accWithdraw = new AgyWithdraw();
        accWithdraw.setStatus(Constants.IsStatus.succeed);
        accWithdraw.setPassTime(getCurrentDate(FORMAT_10_DATE));
        accWithdraw.setLoginSysUserName(loginSysUserName);
        return withdrawMapper.accSumDrawingAmount(accWithdraw);
    }

    public AgyWithdraw queryAccObject(Integer id) {
        AgyWithdraw accWithdraw = new AgyWithdraw();
        accWithdraw.setId(id);
        Optional<AgyWithdraw> optional = Optional.ofNullable(
                withdrawMapper.findAccWithdrawList(accWithdraw)
                        .stream().findAny()).get();
        if (optional.isPresent()) {
            AgyWithdraw accWithdraw1 = optional.get();
            accWithdraw1.setMbrBankcard(agyBankCardMapper.
                    selectByPrimaryKey(accWithdraw1.getBankCardId()));
            AgyWithdraw accCount = new AgyWithdraw();
            accCount.setNotStatus(Constants.IsStatus.defeated);
            accCount.setAccountId(accWithdraw1.getAccountId());
            accCount.setCreateTimeTo(accWithdraw1.getCreateTime());
            accWithdraw1.setWithdrawCount(withdrawMapper.findAccWithdrawCount(accCount));
            return accWithdraw1;
        }
        return null;
    }

    public void checkoutStatusByTwo(Integer id) {
        AgyWithdraw withdraw = agyWithdrawMapper.selectByPrimaryKey(id);
        if (!withdraw.getStatus().equals(Constants.IsStatus.pending)
                && !withdraw.getStatus().equals(Constants.IsStatus.four)) {
            throw new R200Exception("???????????????");
        }
    }

    public void updateAccStatus(AgyWithdraw accWithdraw, String loginName, BizEvent bizEvent, String ip) {
        String key = RedisConstants.AGENT_WITHDRAW_AUDIT + CommonUtil.getSiteCode() + accWithdraw.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accWithdraw.getId(), 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("???????????????????????????????????????");
        }
        try {
            // ??????????????????
            AgyWithdraw withdraw = checkoutFund(accWithdraw.getId());
            //setBizEvent(withdraw, bizEvent, accWithdraw, loginName);
            // ??????????????????/???????????????
            // 1. ????????????/?????? ??????????????????
            if (withdraw.getStatus() == Constants.EVNumber.two) {
                // ??????????????????
                if (!StringUtil.isEmpty(accWithdraw.getMemo())) {
                    withdraw.setMemo(accWithdraw.getMemo());
                }
            }
            // 2. ????????????/?????? ??????????????????
            if (withdraw.getStatus() == Constants.EVNumber.three) {
                // ??????????????????
                if (!StringUtil.isEmpty(accWithdraw.getMemoWithdraw())) {
                    withdraw.setMemoWithdraw(accWithdraw.getMemoWithdraw());
                }
            }

            withdraw.setModifyUser(loginName);
            withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            // ????????????
            if (accWithdraw.getStatus() == Constants.EVNumber.one
                    && withdraw.getStatus() == Constants.EVNumber.three) {
                withdraw.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setPassUser(loginName);
                withdraw.setStatus(accWithdraw.getStatus());
            }
            // ????????????
            if (accWithdraw.getStatus() == Constants.EVNumber.one
                    && withdraw.getStatus() == Constants.EVNumber.two) {
                // 1??????????????????????????????
                if (System.currentTimeMillis() - DateUtil.parse(withdraw.getCreateTime(), FORMAT_18_DATE_TIME).getTime() <= 60000) {
                    throw new R200Exception("??????1???????????????????????????");
                }

                withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setAuditUser(loginName);
                withdraw.setStatus(Constants.EVNumber.three);
                // ???????????????????????????
                withdraw.setLockStatus(Constants.EVNumber.zero);
                withdraw.setLockOperator(null);
                withdraw.setLastLockTime(null);

                beginMerchantPayment(withdraw);
                return;
            }
            // ??????/?????? ??????
            if (accWithdraw.getStatus() == Constants.EVNumber.zero
                    && (withdraw.getStatus() == Constants.EVNumber.two
                    || withdraw.getStatus() == Constants.EVNumber.three)) {
                withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setAuditUser(loginName);
                withdraw.setStatus(accWithdraw.getStatus());
                setAccWithdrawZreo(withdraw);
            }
            // ??????????????????????????? <-- ??????/???????????? + ????????????
            withdraw.setLockStatus(Constants.EVNumber.zero);
            withdraw.setLockOperator(null);
            withdraw.setLastLockTime(null);
            agyWithdrawMapper.updateByPrimaryKey(withdraw);
        } finally {
            redisService.del(key);
        }
    }

    private void setAccWithdrawZreo(AgyWithdraw accWithdraw) {
        AgyBillDetail billDetail = billDetailMapper.selectByPrimaryKey(accWithdraw.getBillDetailId());
        if (Objects.nonNull(billDetail)) {
            AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(accWithdraw.getAccountId());
            AgyWallet agyWallet1 = walletService.setAgyWallet(agentAccount, accWithdraw.getDrawingAmount(),
                    OrderConstants.AGENT_ORDER_CODE_ATH, agentAccount.getId(),
                    null, accWithdraw.getOrderNo(), agentAccount.getAgyAccount(), Constants.EVNumber.zero);
            walletService.addWalletAndBillDetail(agyWallet1, Constants.EVNumber.one);
        }
    }

    private FundMerchantPay getFundMerchantPay(AgyWithdraw withDraw) {
        FundMerchantPay merchantPay = new FundMerchantPay();
        merchantPay.setMethodType(withDraw.getMethodType());
        merchantPay.setAvailable(Constants.EVNumber.one);

        if (Constants.EVNumber.one == withDraw.getMethodType().intValue()) {    // ????????????
            AgentCryptoCurrencies mbrCryptoCurrencies = cryptoCurrenciesMapper.selectByPrimaryKey(withDraw.getCryptoCurrenciesId());
            merchantPay.setCurrencyCode(mbrCryptoCurrencies.getCurrencyCode());
            merchantPay.setCurrencyProtocol(mbrCryptoCurrencies.getCurrencyProtocol());
        }
        return merchantPayMapper.selectOne(merchantPay);
    }

    private void beginMerchantPayment(AgyWithdraw withDraw) {
        // ???????????????????????????????????????
        Boolean isPayment = checkoutMerchantPayment(withDraw);
        if (Boolean.FALSE.equals(isPayment)) {
            withDraw.setType(Constants.EVNumber.zero);
            agyWithdrawMapper.updateByPrimaryKey(withDraw);
            return;
        }
        // ????????????????????????
        FundMerchantPay merchantPay = getFundMerchantPay(withDraw);
        TChannelPay platform = channelPayMapper.selectByPrimaryKey(merchantPay.getChannelId());
        if (Constants.EVNumber.one == withDraw.getMethodType().intValue()) {    // ????????????
            evelletPayment(withDraw, merchantPay);
            return;
        }
        withDraw.setType(Constants.EVNumber.zero);
        agyWithdrawMapper.updateByPrimaryKey(withDraw);
        return;
    }

    private void evelletPayment(AgyWithdraw withDraw, FundMerchantPay merchantPay) {
        log.info("agent evelletPayment==createuser==" + withDraw.getCreateUser() + "==order==" + withDraw.getOrderNo());

        AgentAccount account = agentAccountMapper.selectByPrimaryKey(withDraw.getAccountId());
        AgentCryptoCurrencies mbrCryptoCurrencies = cryptoCurrenciesMapper.selectByPrimaryKey(withDraw.getCryptoCurrenciesId());

        String result = cryptoCurrenciesService.evelletPayment(merchantPay,
                withDraw.getOrderNo(), withDraw.getActualArrivalCr(), account.getAgyAccount(),
                mbrCryptoCurrencies.getWalletAddress(), withDraw.getCreateUser(), Constants.TYPE_AGENT);
        if (StringUtils.isEmpty(result)) {
            // ????????????????????????????????????????????????????????????
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "??????evellet????????????null,??????????????????????????????");
            return;
        }
        CommonEvelletResponse response = new Gson().fromJson(result, CommonEvelletResponse.class);
        if (isNull(response) || isNull(response.getCode())) {
            // ????????????????????????????????????????????????????????????
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "??????evellet????????????????????????,??????????????????????????????");
            return;
        }
        // ????????????
        if (Integer.valueOf("200").equals(response.getCode())) {
            log.info("agent evelletPayment==createuser==" + withDraw.getCreateUser() + "==order==" + withDraw.getOrderNo() + "????????????");
            succeedMerchantPay(merchantPay, withDraw, "1", null, withDraw.getOrderNo());
        } else {  // ????????????
            // ?????????????????????
            log.info("agent evelletPayment==createuser==" + withDraw.getCreateUser() + "==order==" + withDraw.getOrderNo() + "????????????");
            withDraw.setMemo("??????evellet????????????,???????????????" + response.getMsg());
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }

    }

    private void panZiPayment(AgyWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        String bankCode = MerchantPayConstants.pzMerchantPayMap.get(bankcard.getBankName());
        if (StringUtil.isEmpty(bankCode) || StringUtil.isEmpty(bankcard.getRealName())
                || StringUtil.isEmpty(bankcard.getCardNo())) {
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        PZQueryResponseDto responseDto = panZiPayService.debitPayment(withDraw.getOrderNo(),
                bankCode, bankcard, withDraw.getActualArrival(), merchantPay);
        if (isNull(responseDto)) {
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "??????????????????null,??????????????????????????????");
            return;
        }
        if (Boolean.TRUE.equals(responseDto.getSuccess())) {
            PZPaymentResponseDto content = jsonUtil.fromJson(jsonUtil.toJson(responseDto.getContent()), PZPaymentResponseDto.class);
            succeedMerchantPay(merchantPay, withDraw, "1", null, content.getOut_trade_no());
        } else {
            updateMerchantPaymentWithdraw(withDraw);
        }
    }

    private void onePayPayment(AgyWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        String siteCode = CommonUtil.getSiteCode();
        String orderNo = withDraw.getOrderNo();
        OnePayResponseDto responseDto = onePayService.debitPayment(orderNo, bankcard, withDraw.getActualArrival(), merchantPay, siteCode);
        if (isNull(responseDto)) {
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "??????onePay????????????null,??????????????????????????????");
        } else if ("0".equals(responseDto.getStatus())) {//????????????
            succeedMerchantPay(merchantPay, withDraw, "1", responseDto.getOrdersid(), withDraw.getOrderNo());
        } else {//????????????
            if (StringUtil.isEmpty(withDraw.getMemo())) {
                withDraw.setMemo("??????????????????,???????????????" + responseDto.getMsg());
            } else {
                withDraw.setMemo(withDraw.getMemo() + ">>??????????????????,???????????????" + responseDto.getMsg());
            }
            updateMerchantPaymentWithdraw(withDraw);
            log.info(withDraw.getOrderNo() + "??????????????????,???????????????" + responseDto.getMsg());
        }
    }

    // Payment ??????
    private void paymentPayment(AgyWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        // ??????????????????????????????
        String bankCode = MerchantPayConstants.paymentMerchantPayMap.get(bankcard.getBankName());
        if (StringUtil.isNotEmpty(bankCode)) {
            withDraw.setMemo("Payment?????????????????????????????????");
            // ?????????????????????
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        // ????????????????????????????????????
        if (StringUtil.isEmpty(bankcard.getRealName()) || StringUtil.isEmpty(bankcard.getCardNo())) {
            withDraw.setMemo("Payment???????????????????????????????????????");
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        // ??????????????????: ?????????????????????????????????????????????????????????????????????????????????????????????????????????
        String siteCode = CommonUtil.getSiteCode();
        String orderNo = withDraw.getOrderNo();
        String result = paymentPayService.debitPayment(orderNo, bankcard, withDraw.getActualArrival(), merchantPay, siteCode);
        if (StringUtils.isEmpty(result)) {
            // ????????????????????????????????????????????????????????????
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "??????Payment????????????null,??????????????????????????????");
            return;
        }
        PaymentPayBaseResponseDto response = jsonUtil.fromJson(result, PaymentPayBaseResponseDto.class);
        if (isNull(response) || isNull(response.getStatus())) {
            // ????????????????????????????????????????????????????????????
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "??????Payment????????????????????????,??????????????????????????????");
            return;
        }
        // ??????????????????
        if (!Integer.valueOf(Constants.EVNumber.one).equals(response.getStatus())) {
            // ?????????????????????????????????2/??????????????????4/????????????5/????????????7/????????????10/11/12/14/15/16/17/19/21
            if (Integer.valueOf(MerchantPayConstants.paymentStatusCode.two).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.four).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.five).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.seven).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.ten).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.eleven).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.twentyone).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.twelve).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.fourteen).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.fiveteen).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.sixteen).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.seventeen).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.nineteen).equals(response.getStatus())) {

                // ?????????????????????
                withDraw.setMemo("??????Payment????????????,???????????????" + response.getMsg() + "=????????????" + response.getStatus());
                updateMerchantPaymentWithdraw(withDraw);
                return;
            }

            // ???????????????????????????  ????????????  // ????????????/???????????????/?????????/????????????/????????????/??????????????????....
            // ????????????????????????????????????????????????????????????
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "??????Payment??????????????????,????????????????????????????????????????????????" + response.getMsg() + "=????????????" + response.getStatus());
            return;

        } else {  // ????????????
            PaymentPayExecuteResponseDto paymentPayExecuteResponseDto = jsonUtil.fromJson(jsonUtil.toJson(response.getData()), PaymentPayExecuteResponseDto.class);
            String orderid = "";
            if (Objects.isNull(paymentPayExecuteResponseDto) || StringUtils.isEmpty(paymentPayExecuteResponseDto.getEventNumber())) {
                orderid = "Payment???????????????????????????";
            } else {
                orderid = paymentPayExecuteResponseDto.getEventNumber();
            }
            succeedMerchantPay(merchantPay, withDraw, "1", orderid, withDraw.getOrderNo());
        }
    }

    // LBT??????
    private void lbtPayment(AgyWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        // ????????????????????????????????????
        if (StringUtil.isEmpty(bankcard.getRealName()) || StringUtil.isEmpty(bankcard.getCardNo())) {
            withDraw.setMemo("Payment???????????????????????????????????????");
            // ?????????????????????
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        // ??????????????????: ?????????????????????????????????????????????????????????????????????????????????????????????????????????
        //---------------
        //String result = lbtPayService.debitPayment(bankcard, withDraw, merchantPay);
        String result = null;
        //----------------
        if (StringUtils.isEmpty(result)) {
            // ????????????????????????????????????????????????????????????
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "??????LBT????????????null,??????????????????????????????");
            return;
        }
        LBTReponseDto response = jsonUtil.fromJson(result, LBTReponseDto.class);
        if (isNull(response)) {
            // ????????????????????????????????????????????????????????????
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "??????LBT????????????????????????,??????????????????????????????!");
            return;
        }
        if (Objects.isNull(response.getCode())) {
            // ????????????????????????????????????????????????????????????
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "??????LBT????????????????????????,??????????????????????????????!!");
            return;
        }
        if (Integer.valueOf(200).equals(response.getCode())) {    // ??????
            // ??????
            succeedMerchantPay(merchantPay, withDraw, "1", null, withDraw.getOrderNo());
            log.info("LBT??????==outTradeNo==" + withDraw.getOrderNo() + "==??????");
        } else {
            // ?????????????????????
            withDraw.setMemo("??????LBT????????????,???????????????" + response.getMsg() + "=????????????" + response.getCode() + "=msg=" + response.getMsg());
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param merchantPay
     * @param withDraw
     * @param bankStatus
     * @param orderId
     * @param transId
     */
    private void succeedMerchantPay(FundMerchantPay merchantPay, AgyWithdraw withDraw, String bankStatus, String orderId, String transId) {
        AgentMerchantDetail merchantDetail = new AgentMerchantDetail();
        merchantDetail.setMerchantId(merchantPay.getId());
        merchantDetail.setMerchantName(merchantPay.getMerchantName());
        merchantDetail.setMerchantNo(merchantPay.getMerchantNo());
        merchantDetail.setBankStatus(bankStatus);
        merchantDetail.setOrderId(orderId);
        merchantDetail.setTransId(transId);
        merchantDetail.setAccWithdrawId(withDraw.getId());
        withDraw.setStatus("2".equals(bankStatus) || "SUCCESS".equals(bankStatus) ?
                Constants.EVNumber.one : Constants.EVNumber.five);
        if ("2".equals(bankStatus) || "SUCCESS".equals(bankStatus)) {
            withDraw.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
            withDraw.setPassUser(Constants.SYSTEM_PASSUSER);
        }
        withDraw.setType(Constants.EVNumber.one);
        agyWithdrawMapper.updateByPrimaryKey(withDraw);
        merchantDetailMapper.insert(merchantDetail);
    }

    private void updateMerchantPaymentWithdraw(AgyWithdraw withDraw) {
        withDraw.setStatus(Constants.EVNumber.three);
        withDraw.setType(Constants.EVNumber.zero);
        agyWithdrawMapper.updateByPrimaryKey(withDraw);
    }

    private void updateWithdrawErroMsg(AgyWithdraw withDraw, Integer type, String memo) {
        withDraw.setStatus(Constants.EVNumber.five);
        withDraw.setType(type);
        if (StringUtil.isEmpty(withDraw.getMemo())) {
            withDraw.setMemo(memo);
        } else {
            withDraw.setMemo(withDraw.getMemo() + ">>" + memo);
        }
        agyWithdrawMapper.updateByPrimaryKey(withDraw);
    }

    private Boolean checkoutMerchantPayment(AgyWithdraw withDraw) {
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.PAY_AUTOMATIC);
        SysSetting moneySetting = sysSettingService.getSysSetting(SystemConstants.PAY_MONEY);
        if (isNull(setting) || "0".equals(setting.getSysvalue())) {
            return Boolean.FALSE;
        }
        if (nonNull(moneySetting) && withDraw.getDrawingAmount()
                .compareTo(new BigDecimal(moneySetting.getSysvalue())) == 1) {
            return Boolean.FALSE;
        }

     /*   int count = withdrawMapper.findMerchantPayCount(withDraw.getAccountId());
        if (count == 0) {
            return Boolean.FALSE;
        }*/
        FundMerchantPay merchantPay = getFundMerchantPay(withDraw);
        if (isNull(merchantPay) || !merchantPay.getDevSource().contains(withDraw.getWithdrawSource().toString())) {
            return Boolean.FALSE;
        }
        TChannelPay channelPay = channelPayMapper.selectByPrimaryKey(merchantPay.getChannelId());
        if (isNull(channelPay) || channelPay.getAvailable() == Constants.EVNumber.zero) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Async("getPayResultExecutor")
    public void updateMerchantPayment(AgyWithdraw withdraw, String siteCode) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        String redisKey = RedisConstants.UPDATE_WITHDRAW + siteCode + withdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, withdraw.getId(), 200, TimeUnit.SECONDS);
        if (flag) {
            AgyWithdraw accWithdraw = agyWithdrawMapper.selectByPrimaryKey(withdraw.getId());
            if (accWithdraw.getStatus() == Constants.EVNumber.five) {   // 5??????????????? ?????????????????????????????????????????????????????????????????????????????????????????????detail.
                // ?????????????????????????????????
                FundMerchantPay merchantPay = withdrawMapper.getMerchantPayByOrderno(withdraw.getOrderNo());
                if (Objects.isNull(merchantPay)) {  // ?????????????????????
                    return;
                }
                TChannelPay platform = channelPayMapper.selectByPrimaryKey(merchantPay.getChannelId());
                // EVELLET
                if (MerchantPayConstants.EVELLET_PAY.equals(platform.getPlatformCode())) {
                    updateEvelltetPayMerchantPayment(merchantPay, withdraw, siteCode);
                }
                // LBT
                if (MerchantPayConstants.LBT_PAY.equals(platform.getPlatformCode())) {
                    updateLBTPayMerchantPayment(merchantPay, withdraw, siteCode);
                }
            }
            redisService.del(redisKey);
        }
    }


    // LBT????????????????????????
    private void updateLBTPayMerchantPayment(FundMerchantPay merchantPay, AgyWithdraw as, String siteCode) {
        //---------
        //String result = lbtPayService.querySubmitSuccess(as, merchantPay);
        String result = null;
        //-----------
        if (StringUtil.isNotEmpty(result)) {
            LBTQueryReponseDto response = jsonUtil.fromJson(result, LBTQueryReponseDto.class);
            if (Objects.nonNull(response) && Objects.nonNull(response.getStatus())) {
                // ????????????
                if ("APPROVED".equals(response.getStatus())) {
                    log.info("LBT??????==outTradeNo==" + as.getOrderNo() + "==????????????==??????");
                    String remarks = response.getRemarks();
                    if (StringUtil.isEmpty(remarks)) {
                        remarks = "LBT?????????????????????";
                    }
                    updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2", remarks, null);
                }

                if ("REJECTED".equals(response.getStatus())) {    // LBT????????????????????????
                    log.info("LBT??????==outTradeNo==" + as.getOrderNo() + "==????????????==??????");
                    AgyWithdraw withdraw = agyWithdrawMapper.selectByPrimaryKey(as.getId());
                    // ????????????
                    withdraw.setModifyUser(Constants.SYSTEM_USER);
                    withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    withdraw.setAuditUser(Constants.SYSTEM_USER);
                    withdraw.setStatus(Constants.EVNumber.zero);    // ????????????
                    String remarks = response.getRemarks();
                    if (StringUtil.isEmpty(remarks)) {
                        remarks = "LBT?????????????????????";
                    }
                    withdraw.setMemoWithdraw(remarks); // ????????????
                    // ????????????
                    setAccWithdrawZreo(withdraw);
                    // ???????????????
                    agyWithdrawMapper.updateByPrimaryKey(withdraw);
                    // ????????????????????????
                    sendWithdrawRefuseMsg(siteCode, as.getAccountId(), as.getActualArrival());
                    // ?????????????????????
                }
            }
        }
    }

    // EVELLET????????????????????????
    private void updateEvelltetPayMerchantPayment(FundMerchantPay merchantPay, AgyWithdraw as, String siteCode) {
        //------------
        //String result = cryptoCurrenciesService.querySubmitSuccess(as, merchantPay);
        String result = null;
        //--------------
        if (StringUtil.isNotEmpty(result)) {
            EvelletPayTransferCallbackDto response = jsonUtil.fromJson(result, EvelletPayTransferCallbackDto.class);
            if (Objects.nonNull(response) && Objects.nonNull(response.getStatus())) {
                // ????????????
                if (Integer.valueOf(Constants.EVNumber.two).equals(response.getStatus())) {
                    log.info("querySubmitSuccess==createuser==" + as.getCreateUser() + "==order==" + as.getOrderNo()
                            + "==????????????==??????");
                    updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2", null, response.getHash());
                }
                // ????????????
                if (Integer.valueOf(Constants.EVNumber.three).equals(response.getStatus())) {
                    log.info("querySubmitSuccess==createuser==" + as.getCreateUser() + "==order==" + as.getOrderNo()
                            + "==????????????==??????");
                    updateMerchantPaymentStatus(Constants.EVNumber.three, as.getId(), as.getMerchantDetailId(), "3", null, null);
                }
            }
        }
    }

    private void sendWithdrawRefuseMsg(String siteCode, Integer accountId, BigDecimal withdrawMoney) {
        BizEvent bizEvent = new BizEvent(this, siteCode, accountId, BizEventType.MEMBER_WITHDRAWAL_REVIEW_VERIFY_FAILED);
        bizEvent.setWithdrawMoney(withdrawMoney);
        applicationEventPublisher.publishEvent(bizEvent);
    }

    /**
     * ????????????????????????????????????
     *
     * @param status
     * @param accWithdrawId
     * @param merchantDetailId
     * @param bankStatus
     */
    public void updateMerchantPaymentStatus(int status, int accWithdrawId, int merchantDetailId, String
            bankStatus, String memo, String hash) {
        AgyWithdraw accWithdraw = new AgyWithdraw();
        accWithdraw.setStatus(status);
        accWithdraw.setId(accWithdrawId);
        accWithdraw.setHash(hash);
        if (StringUtil.isNotEmpty(memo)) {
            accWithdraw.setMemoWithdraw(memo);  // ????????????
        }
        if (status == Constants.EVNumber.three) {
            accWithdraw.setType(Constants.EVNumber.zero);
        }
        if (status == Constants.EVNumber.one) {
            accWithdraw.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
            accWithdraw.setPassUser(Constants.SYSTEM_PASSUSER);
        }
        agyWithdrawMapper.updateByPrimaryKeySelective(accWithdraw);
        AgentMerchantDetail merchantDetail = new AgentMerchantDetail();
        merchantDetail.setId(merchantDetailId);
        merchantDetail.setBankStatus(bankStatus);
        merchantDetailMapper.updateByPrimaryKeySelective(merchantDetail);
    }

    private AgyWithdraw checkoutFund(Integer id) {
        AgyWithdraw withdraw = agyWithdrawMapper.selectByPrimaryKey(id);
        if (withdraw.getStatus().equals(Constants.IsStatus.succeed)
                || withdraw.getStatus().equals(Constants.IsStatus.defeated)) {
            throw new R200Exception(messagesConfig.getValue("saasops.illegal.request"));
        }
        if (nonNull(withdraw.getType())) {
            if (withdraw.getStatus() == Constants.EVNumber.two
                    && withdraw.getType() == Constants.EVNumber.three) {
                throw new R200Exception("?????????????????????????????????");
            }
            if (withdraw.getStatus() == Constants.EVNumber.five) {
                throw new R200Exception("???????????????????????????????????????????????????");
            }
        }
        return withdraw;
    }

    public void updateAccMemo(Integer id, String memo, String loginName) {
        AgyWithdraw withdraw = agyWithdrawMapper.selectByPrimaryKey(id);
        if (withdraw.getStatus() == Constants.EVNumber.one) {
            throw new R200Exception("??????????????????????????????????????????");
        }
        withdraw.setMemo(memo);
        withdraw.setModifyUser(loginName);
        withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agyWithdrawMapper.updateByPrimaryKey(withdraw);
    }

    /**
     * ??????????????????
     *
     * @param accWithdraw
     * @return
     */
    public List<CountEntity> withdrawCountByStatus(AgyWithdraw accWithdraw) {
        return withdrawMapper.withdrawCountByStatus(accWithdraw);
    }

    public List<AgyWithdraw> fundAccWithdrawMerchant(Integer accountId) {
        return withdrawMapper.fundAccWithdrawMerchant(accountId);
    }

    public AgyWithdraw lockstatus(AgyWithdraw accWithdraw, String userName) {
        // ???????????????????????????
        updateAllLockStatus();
        // ?????????????????????
        AgyWithdraw acc = this.queryObject(accWithdraw.getId());
        // ??????????????????
        if (Integer.valueOf(Constants.EVNumber.one).equals(acc.getLockStatus())) {    // ????????????
            if (userName.equals(acc.getLockOperator())) {     // ?????????????????????
                acc.setIsCurrentUserLock(Constants.EVNumber.one);
            } else {  // ?????????????????????
                acc.setIsCurrentUserLock(Constants.EVNumber.two);
            }
        } else {      // ???????????????
            acc.setIsCurrentUserLock(Constants.EVNumber.zero);
        }

        return acc;
    }

    public void lock(AgyWithdraw accWithdraw, String userName) {
        AgyWithdraw acc = new AgyWithdraw();
        acc.setId(accWithdraw.getId());
        acc.setLockStatus(accWithdraw.getLockStatus());
        acc.setLockOperator(userName);
        acc.setLastLockTime(getCurrentDate(FORMAT_18_DATE_TIME));
        this.update(acc);
    }

    public void unLock(AgyWithdraw accWithdraw) {
        AgyWithdraw acc = new AgyWithdraw();
        acc.setId(accWithdraw.getId());
        acc.setLockStatus(accWithdraw.getLockStatus());
        acc.setLockOperator(null);
        acc.setLastLockTime(null);
        this.update(acc);
    }

    public void updateAllLockStatus() {
        withdrawMapper.updateAllLockStatus();
    }
}

