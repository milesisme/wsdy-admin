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
        //判断statuss是否为空
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
            throw new R200Exception("请刷新数据");
        }
    }

    public void updateAccStatus(AgyWithdraw accWithdraw, String loginName, BizEvent bizEvent, String ip) {
        String key = RedisConstants.AGENT_WITHDRAW_AUDIT + CommonUtil.getSiteCode() + accWithdraw.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accWithdraw.getId(), 200, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复点击！");
        }
        try {
            // 获得当前记录
            AgyWithdraw withdraw = checkoutFund(accWithdraw.getId());
            //setBizEvent(withdraw, bizEvent, accWithdraw, loginName);
            // 增加处理初审/复审的逻辑
            // 1. 初审通过/拒绝 更新初审备注
            if (withdraw.getStatus() == Constants.EVNumber.two) {
                // 允许备注为空
                if (!StringUtil.isEmpty(accWithdraw.getMemo())) {
                    withdraw.setMemo(accWithdraw.getMemo());
                }
            }
            // 2. 复审通过/拒绝 更新复审备注
            if (withdraw.getStatus() == Constants.EVNumber.three) {
                // 允许备注为空
                if (!StringUtil.isEmpty(accWithdraw.getMemoWithdraw())) {
                    withdraw.setMemoWithdraw(accWithdraw.getMemoWithdraw());
                }
            }

            withdraw.setModifyUser(loginName);
            withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            // 复审通过
            if (accWithdraw.getStatus() == Constants.EVNumber.one
                    && withdraw.getStatus() == Constants.EVNumber.three) {
                withdraw.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setPassUser(loginName);
                withdraw.setStatus(accWithdraw.getStatus());
            }
            // 初审通过
            if (accWithdraw.getStatus() == Constants.EVNumber.one
                    && withdraw.getStatus() == Constants.EVNumber.two) {
                // 1分钟内不允许初审通过
                if (System.currentTimeMillis() - DateUtil.parse(withdraw.getCreateTime(), FORMAT_18_DATE_TIME).getTime() <= 60000) {
                    throw new R200Exception("提款1分钟内无法通过初审");
                }

                withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setAuditUser(loginName);
                withdraw.setStatus(Constants.EVNumber.three);
                // 更新操作锁定：解锁
                withdraw.setLockStatus(Constants.EVNumber.zero);
                withdraw.setLockOperator(null);
                withdraw.setLastLockTime(null);

                beginMerchantPayment(withdraw);
                return;
            }
            // 初审/复审 拒绝
            if (accWithdraw.getStatus() == Constants.EVNumber.zero
                    && (withdraw.getStatus() == Constants.EVNumber.two
                    || withdraw.getStatus() == Constants.EVNumber.three)) {
                withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                withdraw.setAuditUser(loginName);
                withdraw.setStatus(accWithdraw.getStatus());
                setAccWithdrawZreo(withdraw);
            }
            // 更新操作锁定：解锁 <-- 初审/复审拒绝 + 复审通过
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

        if (Constants.EVNumber.one == withDraw.getMethodType().intValue()) {    // 加密货币
            AgentCryptoCurrencies mbrCryptoCurrencies = cryptoCurrenciesMapper.selectByPrimaryKey(withDraw.getCryptoCurrenciesId());
            merchantPay.setCurrencyCode(mbrCryptoCurrencies.getCurrencyCode());
            merchantPay.setCurrencyProtocol(mbrCryptoCurrencies.getCurrencyProtocol());
        }
        return merchantPayMapper.selectOne(merchantPay);
    }

    private void beginMerchantPayment(AgyWithdraw withDraw) {
        // 判断该会员是否符合代付配置
        Boolean isPayment = checkoutMerchantPayment(withDraw);
        if (Boolean.FALSE.equals(isPayment)) {
            withDraw.setType(Constants.EVNumber.zero);
            agyWithdrawMapper.updateByPrimaryKey(withDraw);
            return;
        }
        // 获取代付渠道配置
        FundMerchantPay merchantPay = getFundMerchantPay(withDraw);
        TChannelPay platform = channelPayMapper.selectByPrimaryKey(merchantPay.getChannelId());
        if (Constants.EVNumber.one == withDraw.getMethodType().intValue()) {    // 加密货币
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
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交evellet代付返回null,程序无法判断是否成功");
            return;
        }
        CommonEvelletResponse response = new Gson().fromJson(result, CommonEvelletResponse.class);
        if (isNull(response) || isNull(response.getCode())) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交evellet代付返回数据错误,程序无法判断是否成功");
            return;
        }
        // 提单成功
        if (Integer.valueOf("200").equals(response.getCode())) {
            log.info("agent evelletPayment==createuser==" + withDraw.getCreateUser() + "==order==" + withDraw.getOrderNo() + "提单成功");
            succeedMerchantPay(merchantPay, withDraw, "1", null, withDraw.getOrderNo());
        } else {  // 提单失败
            // 变更为手动出款
            log.info("agent evelletPayment==createuser==" + withDraw.getCreateUser() + "==order==" + withDraw.getOrderNo() + "提单失败");
            withDraw.setMemo("提交evellet代付失败,状态描述：" + response.getMsg());
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
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交代付返回null,程序无法判断是否成功");
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
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交onePay代付返回null,程序无法判断是否成功");
        } else if ("0".equals(responseDto.getStatus())) {//提交成功
            succeedMerchantPay(merchantPay, withDraw, "1", responseDto.getOrdersid(), withDraw.getOrderNo());
        } else {//提交失败
            if (StringUtil.isEmpty(withDraw.getMemo())) {
                withDraw.setMemo("提交代付失败,失败原因：" + responseDto.getMsg());
            } else {
                withDraw.setMemo(withDraw.getMemo() + ">>提交代付失败,失败原因：" + responseDto.getMsg());
            }
            updateMerchantPaymentWithdraw(withDraw);
            log.info(withDraw.getOrderNo() + "提交代付失败,失败原因：" + responseDto.getMsg());
        }
    }

    // Payment 代付
    private void paymentPayment(AgyWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        // 校验：不支持中国邮政
        String bankCode = MerchantPayConstants.paymentMerchantPayMap.get(bankcard.getBankName());
        if (StringUtil.isNotEmpty(bankCode)) {
            withDraw.setMemo("Payment代付：不支持邮政银行！");
            // 变更为手动出款
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        // 校验：银行账号信息不完整
        if (StringUtil.isEmpty(bankcard.getRealName()) || StringUtil.isEmpty(bankcard.getCardNo())) {
            withDraw.setMemo("Payment代付：银行账号信息不完整！");
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        // 提交代付申请: 提单了，不管结果怎样，都是自动出款、自动出款中，等回调或者让人工去干涉
        String siteCode = CommonUtil.getSiteCode();
        String orderNo = withDraw.getOrderNo();
        String result = paymentPayService.debitPayment(orderNo, bankcard, withDraw.getActualArrival(), merchantPay, siteCode);
        if (StringUtils.isEmpty(result)) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交Payment代付返回null,程序无法判断是否成功");
            return;
        }
        PaymentPayBaseResponseDto response = jsonUtil.fromJson(result, PaymentPayBaseResponseDto.class);
        if (isNull(response) || isNull(response.getStatus())) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交Payment代付返回数据错误,程序无法判断是否成功");
            return;
        }
        // 错误状态处理
        if (!Integer.valueOf(Constants.EVNumber.one).equals(response.getStatus())) {
            // 判断不成功的：参数错误2/订单号已存在4/验签失败5/支付失败7/余额不足10/11/12/14/15/16/17/19/21
            if (Integer.valueOf(MerchantPayConstants.paymentStatusCode.two).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.four).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.five).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.seven).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.ten).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.eleven).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.twentyone).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.twelve).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.fourteen).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.fiveteen).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.sixteen).equals(response.getStatus())
                    || Integer.valueOf(MerchantPayConstants.paymentStatusCode.seventeen).equals(response.getStatus()) || Integer.valueOf(MerchantPayConstants.paymentStatusCode.nineteen).equals(response.getStatus())) {

                // 变更为手动出款
                withDraw.setMemo("提交Payment代付失败,状态描述：" + response.getMsg() + "=状态码：" + response.getStatus());
                updateMerchantPaymentWithdraw(withDraw);
                return;
            }

            // 无法判断是否成功的  其他的：  // 未知错误/订单未处理/处理中/触发风控/未知异常/订单号不存在....
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交Payment代付返回失败,程序无法判断是否成功，状态描述：" + response.getMsg() + "=状态码：" + response.getStatus());
            return;

        } else {  // 提单成功
            PaymentPayExecuteResponseDto paymentPayExecuteResponseDto = jsonUtil.fromJson(jsonUtil.toJson(response.getData()), PaymentPayExecuteResponseDto.class);
            String orderid = "";
            if (Objects.isNull(paymentPayExecuteResponseDto) || StringUtils.isEmpty(paymentPayExecuteResponseDto.getEventNumber())) {
                orderid = "Payment代付事件单号返回空";
            } else {
                orderid = paymentPayExecuteResponseDto.getEventNumber();
            }
            succeedMerchantPay(merchantPay, withDraw, "1", orderid, withDraw.getOrderNo());
        }
    }

    // LBT代付
    private void lbtPayment(AgyWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        // 校验：银行账号信息不完整
        if (StringUtil.isEmpty(bankcard.getRealName()) || StringUtil.isEmpty(bankcard.getCardNo())) {
            withDraw.setMemo("Payment代付：银行账号信息不完整！");
            // 变更为手动出款
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        // 提交代付申请: 提单了，不管结果怎样，都是自动出款、自动出款中，等回调或者让人工去干涉
        //---------------
        //String result = lbtPayService.debitPayment(bankcard, withDraw, merchantPay);
        String result = null;
        //----------------
        if (StringUtils.isEmpty(result)) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交LBT代付返回null,程序无法判断是否成功");
            return;
        }
        LBTReponseDto response = jsonUtil.fromJson(result, LBTReponseDto.class);
        if (isNull(response)) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交LBT代付返回数据错误,程序无法判断是否成功!");
            return;
        }
        if (Objects.isNull(response.getCode())) {
            // 更新为无法判断状态：自动出款、自动出款中
            updateWithdrawErroMsg(withDraw, Constants.EVNumber.one, "提交LBT代付返回数据错误,程序无法判断是否成功!!");
            return;
        }
        if (Integer.valueOf(200).equals(response.getCode())) {    // 成功
            // 成功
            succeedMerchantPay(merchantPay, withDraw, "1", null, withDraw.getOrderNo());
            log.info("LBT代付==outTradeNo==" + withDraw.getOrderNo() + "==成功");
        } else {
            // 变更为手动出款
            withDraw.setMemo("提交LBT代付失败,状态描述：" + response.getMsg() + "=状态码：" + response.getCode() + "=msg=" + response.getMsg());
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
    }

    /**
     * 提交出款代付成功处理
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
            if (accWithdraw.getStatus() == Constants.EVNumber.five) {   // 5自动出款中 的状态才去查询，此处不会查询未代付下单成功的，因为未成功的没有detail.
                // 通过订单号获取代付配置
                FundMerchantPay merchantPay = withdrawMapper.getMerchantPayByOrderno(withdraw.getOrderNo());
                if (Objects.isNull(merchantPay)) {  // 非代付的取款单
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


    // LBT代付查询订单状态
    private void updateLBTPayMerchantPayment(FundMerchantPay merchantPay, AgyWithdraw as, String siteCode) {
        //---------
        //String result = lbtPayService.querySubmitSuccess(as, merchantPay);
        String result = null;
        //-----------
        if (StringUtil.isNotEmpty(result)) {
            LBTQueryReponseDto response = jsonUtil.fromJson(result, LBTQueryReponseDto.class);
            if (Objects.nonNull(response) && Objects.nonNull(response.getStatus())) {
                // 出款成功
                if ("APPROVED".equals(response.getStatus())) {
                    log.info("LBT代付==outTradeNo==" + as.getOrderNo() + "==订单查询==成功");
                    String remarks = response.getRemarks();
                    if (StringUtil.isEmpty(remarks)) {
                        remarks = "LBT代付：审核通过";
                    }
                    updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2", remarks, null);
                }

                if ("REJECTED".equals(response.getStatus())) {    // LBT出款拒绝直接上分
                    log.info("LBT代付==outTradeNo==" + as.getOrderNo() + "==订单查询==失败");
                    AgyWithdraw withdraw = agyWithdrawMapper.selectByPrimaryKey(as.getId());
                    // 处理失败
                    withdraw.setModifyUser(Constants.SYSTEM_USER);
                    withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    withdraw.setAuditUser(Constants.SYSTEM_USER);
                    withdraw.setStatus(Constants.EVNumber.zero);    // 拒绝状态
                    String remarks = response.getRemarks();
                    if (StringUtil.isEmpty(remarks)) {
                        remarks = "LBT代付：审核拒绝";
                    }
                    withdraw.setMemoWithdraw(remarks); // 复审备注
                    // 重新上分
                    setAccWithdrawZreo(withdraw);
                    // 更新存款单
                    agyWithdrawMapper.updateByPrimaryKey(withdraw);
                    // 发送取款拒绝通知
                    sendWithdrawRefuseMsg(siteCode, as.getAccountId(), as.getActualArrival());
                    // 记录操作日志？
                }
            }
        }
    }

    // EVELLET代付查询订单状态
    private void updateEvelltetPayMerchantPayment(FundMerchantPay merchantPay, AgyWithdraw as, String siteCode) {
        //------------
        //String result = cryptoCurrenciesService.querySubmitSuccess(as, merchantPay);
        String result = null;
        //--------------
        if (StringUtil.isNotEmpty(result)) {
            EvelletPayTransferCallbackDto response = jsonUtil.fromJson(result, EvelletPayTransferCallbackDto.class);
            if (Objects.nonNull(response) && Objects.nonNull(response.getStatus())) {
                // 出款成功
                if (Integer.valueOf(Constants.EVNumber.two).equals(response.getStatus())) {
                    log.info("querySubmitSuccess==createuser==" + as.getCreateUser() + "==order==" + as.getOrderNo()
                            + "==订单查询==成功");
                    updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2", null, response.getHash());
                }
                // 出款失败
                if (Integer.valueOf(Constants.EVNumber.three).equals(response.getStatus())) {
                    log.info("querySubmitSuccess==createuser==" + as.getCreateUser() + "==order==" + as.getOrderNo()
                            + "==订单查询==失败");
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
     * 有代付结果后更新出款信息
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
            accWithdraw.setMemoWithdraw(memo);  // 出款备注
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
                throw new R200Exception("该订单正在进行代付处理");
            }
            if (withdraw.getStatus() == Constants.EVNumber.five) {
                throw new R200Exception("该订单已经由代付处理，请勿手工处理");
            }
        }
        return withdraw;
    }

    public void updateAccMemo(Integer id, String memo, String loginName) {
        AgyWithdraw withdraw = agyWithdrawMapper.selectByPrimaryKey(id);
        if (withdraw.getStatus() == Constants.EVNumber.one) {
            throw new R200Exception("已经完成出款，不可以修改备注");
        }
        withdraw.setMemo(memo);
        withdraw.setModifyUser(loginName);
        withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agyWithdrawMapper.updateByPrimaryKey(withdraw);
    }

    /**
     * 会员提款统计
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
        // 先更新所有操作锁定
        updateAllLockStatus();
        // 再查询锁定状态
        AgyWithdraw acc = this.queryObject(accWithdraw.getId());
        // 判断是否锁定
        if (Integer.valueOf(Constants.EVNumber.one).equals(acc.getLockStatus())) {    // 锁定状态
            if (userName.equals(acc.getLockOperator())) {     // 被当前用户锁定
                acc.setIsCurrentUserLock(Constants.EVNumber.one);
            } else {  // 非当前用户锁定
                acc.setIsCurrentUserLock(Constants.EVNumber.two);
            }
        } else {      // 未锁定状态
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

