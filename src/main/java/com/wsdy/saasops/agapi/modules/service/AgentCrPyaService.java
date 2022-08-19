package com.wsdy.saasops.agapi.modules.service;

import com.google.gson.Gson;
import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.pay.dto.CrPayDto;
import com.wsdy.saasops.api.modules.pay.dto.PayParams;
import com.wsdy.saasops.api.modules.pay.dto.evellet.CommonEvelletResponse;
import com.wsdy.saasops.api.modules.pay.dto.evellet.EvelletPayPushCallbackDto;
import com.wsdy.saasops.api.modules.pay.dto.evellet.EvelletPayTradeRequestDto;
import com.wsdy.saasops.api.modules.pay.dto.evellet.EvelletPayTransferCallbackDto;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.ASCIIUtils;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.api.utils.OkHttpUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgentDepositMapper;
import com.wsdy.saasops.modules.agent.dao.AgyWithdrawMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentDeposit;
import com.wsdy.saasops.modules.agent.entity.AgentMerchantDetail;
import com.wsdy.saasops.modules.agent.entity.AgyWithdraw;
import com.wsdy.saasops.modules.agent.mapper.WithdrawMapper;
import com.wsdy.saasops.modules.agent.service.AgentDepositService;
import com.wsdy.saasops.modules.agent.service.AgentWithdrawService;
import com.wsdy.saasops.modules.fund.dao.FundMerchantPayMapper;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.system.pay.dao.SetBasicSysCryptoCurrenciesMapper;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicSysCryptoCurrencies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.common.constants.Constants.SYSTEM_USER;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;

@Slf4j
@Service
public class AgentCrPyaService {

    @Autowired
    private SetBasicSysCryptoCurrenciesMapper setBasicSysCryptoCurrenciesMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Value("${evellet.url}")
    private String evelletUrl;
    @Autowired
    private AgentDepositMapper agentDepositMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgentDepositService agentDepositService;
    @Autowired
    private WithdrawMapper withdrawMapper;
    @Autowired
    private FundMerchantPayMapper merchantPayMapper;
    @Autowired
    private AgyWithdrawMapper agyWithdrawMapper;
    @Autowired
    private AgentWithdrawService withdrawService;


    public CrPayDto qrCrPay(PayParams params, AgentAccount account) {
        SetBasicSysCryptoCurrencies cr = checkoutCrPay(params);

        EvelletPayTradeRequestDto requestDto = new EvelletPayTradeRequestDto();
        // 金额处理 TODO
        Long longFee = params.getFee().multiply(new BigDecimal(10000)).longValue();
        requestDto.setAmount(longFee);
        requestDto.setApplyDate(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        requestDto.setLoginName(account.getAgyAccount());
        requestDto.setUserType(Constants.AGENT);
        // 获得商户号
        requestDto.setMerchantNo(cr.getMerNo());
        // 协议类型
        if (Constants.TYPE_ERC20.equals(cr.getCurrencyProtocol())) {
            requestDto.setType(Constants.TYPE_ERC);
        }
        if (Constants.TYPE_TRC20.equals(cr.getCurrencyProtocol())) {
            requestDto.setType(Constants.TYPE_TRC);
        }

        // 签名
        Map<String, Object> param = jsonUtil.Entity2Map(requestDto);
        param.remove("sign");
        String sign = MD5.getMD5(ASCIIUtils.getFormatUrl(param, cr.getPassword()));
        requestDto.setSign(sign);
        // 下单请求
        String url = evelletUrl + PayConstants.SAASOPS_PAY_EVELLET_PATDO;
        if (!evelletUrl.endsWith("/")) {
            url = evelletUrl + "/" + PayConstants.SAASOPS_PAY_EVELLET_PATDO;
        }

        String jsonMessage;
        try {
            Map<String, String> formMap = jsonUtil.toStringMap(requestDto);
            log.info("agent qrCrPay==createuser==" + account.getLoginName() + "==requestDto==" + formMap + "==url==" + url);
            jsonMessage = OkHttpUtils.postForm(url, formMap);
            log.info("agent qrCrPay==createuser==" + account.getLoginName() + "==qrCrPay返回信息==" + jsonMessage);
        } catch (Exception e) {
            log.info("agent qrCrPay==createuser==" + account.getLoginName() + "==qrCrPay报错==" + e);
            throw new RRException("该支付维护中,请使用其它支付");
        }

        if (Objects.isNull(jsonMessage)) {
            throw new RRException("支付异常！");
        }

        CommonEvelletResponse payResponse = new Gson().fromJson(jsonMessage, CommonEvelletResponse.class);
        if (Objects.isNull(payResponse) || Objects.isNull(payResponse.getCode())) {
            throw new RRException("支付异常！");
        }
        if (payResponse.getCode() != 200) {
            log.info("qrCrPay==createuser==" + account.getLoginName() + "==qrCrPay异常==" + payResponse.getMsg());
            throw new RRException("支付异常！" + payResponse.getMsg());
        }
        // 返回前端数据
        CrPayDto crPayDto = new CrPayDto();
        crPayDto.setCreateTime(requestDto.getApplyDate());
        crPayDto.setDepositAmount(params.getFee()); // 加密货币无手续费下
        BigDecimal depositAmountCNY = CommonUtil.adjustScale(params.getFee().multiply(params.getExchangeRate()));
        crPayDto.setDepositAmountCNY(depositAmountCNY);
        crPayDto.setWalletAddress(payResponse.getAddress());
        crPayDto.setQrCode(payResponse.getQrCode());
        return crPayDto;
    }

    private SetBasicSysCryptoCurrencies checkoutCrPay(PayParams params) {
        SetBasicSysCryptoCurrencies cr = setBasicSysCryptoCurrenciesMapper.selectByPrimaryKey(params.getDepositId());
      /*  if (Objects.isNull(cr) || Constants.EVNumber.zero == cr.getAvailable() || Constants.EVNumber.one == cr.getIsDelete()) {
            throw new R200Exception("此支付方式不接受会员充值");
        }
        if (cr.getMinAmout().compareTo(params.getFee()) == 1) {
            throw new R200Exception("小于单笔最小充值额度");
        }*/
        return cr;
    }


    public String evelletCallback(EvelletPayPushCallbackDto data, String siteCode) {
        // 判断是否存在相同的hash
        AgentDeposit tmp = new AgentDeposit();
        tmp.setHash(data.getHash());
        int count = agentDepositMapper.selectCount(tmp);
        if (count != 0) {
            log.info("agent evelletCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==hash==" + data.getHash() + "该hash已存在!");
            throw new R200Exception("该hash已存在！");
        }
        // 获得交易渠道pay  TODO 此处写死，需要等加密钱包可以给唯一标志
        data.setCurrencyCode("USDT");
        // 协议类型
        if (Constants.TYPE_ERC.equals(data.getType())) {
            data.setCurrencyProtocol(Constants.TYPE_ERC20);
        }
        if (Constants.TYPE_TRC.equals(data.getType())) {
            data.setCurrencyProtocol(Constants.TYPE_TRC20);
        }
        SetBasicSysCryptoCurrencies cr = setBasicSysCryptoCurrenciesMapper.getCrByCodeAndProtocol(data.getCurrencyCode(), data.getCurrencyProtocol());

        // 验签
        Map<String, Object> param = jsonUtil.Entity2Map(data);
        param.remove("sign");
        param.remove("currencyProtocol");
        param.remove("currencyCode");
        String sign = MD5.getMD5(ASCIIUtils.getFormatUrl(param, cr.getPassword()));
        if (!sign.equals(data.getSign())) {
            log.info("agent evelletCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "签名错误");
            throw new R200Exception("验签失败！");
        }

        String key = RedisConstants.EVELLET_CALLBACK_AGENT + siteCode + data.getLoginName() + data.getHash();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, data.getHash(), 360, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isExpired)) {
            log.info("agent evelletCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==重复回调请求");
            throw new R200Exception("重复请求！");
        }
        try {
            // 1. 创建订单
            // 获得会员
            AgentAccount account = new AgentAccount();
            account.setAgyAccount(data.getLoginName());
            account = agentAccountMapper.selectOne(account);
            // 参数准备
            PayParams params = new PayParams();
            params.setOutTradeNo(new SnowFlake().nextId());
            params.setAccountId(account.getId());
            params.setUserName(account.getRealName());
            params.setIp(null);
            params.setFundSource(null);

            params.setExchangeRate(data.getExchangeRate());
            params.setCreateTime(data.getCreateTime());
            params.setHash(data.getHash());
            params.setCrId(cr.getId());    // 加密货币支付渠道id
            // 处理入款金额(币)
            BigDecimal fee = CommonUtil.adjustScale(BigDecimal.valueOf(data.getAmount()).divide(new BigDecimal(10000)));
            params.setFee(fee);
            // 保存订单
            AgentDeposit deposit = saveFundDeposit(params, account);
            // 2. 入款成功后处理钱包等
            AgentDeposit fundDeposit = new AgentDeposit();
            fundDeposit.setId(deposit.getId());
            fundDeposit.setStatus(Constants.EVNumber.one);
            agentDepositService.updateDeposit(fundDeposit, SYSTEM_USER, "");
            return "SUCCESS" + "&" + deposit.getOrderNo();
        } finally {
            redisService.del(key);
        }
    }

    private AgentDeposit saveFundDeposit(PayParams params, AgentAccount account) {
        AgentDeposit deposit = new AgentDeposit();
        deposit.setOrderNo(params.getOutTradeNo() + CommonUtil.genRandom(3, 3));
        deposit.setMark(FundDeposit.Mark.crPay);
        deposit.setStatus(FundDeposit.Status.apply);
        deposit.setIsPayment(FundDeposit.PaymentStatus.unPay);
        deposit.setCrId(params.getCrId());
        BigDecimal depositAmount = params.getFee().multiply(params.getExchangeRate());  // 存款金额RNB
        deposit.setDepositAmount(CommonUtil.adjustScale(depositAmount));    // 截断存款2位数
        deposit.setHandlingCharge(new BigDecimal(0));   // 手续费为空
        deposit.setHandingback(Constants.Available.disable);
        deposit.setActualArrival(deposit.getDepositAmount().add(deposit.getHandlingCharge()));

        deposit.setIp(params.getIp());
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);
        deposit.setDepositUser(account.getLoginName());
        deposit.setCreateUser(account.getLoginName());
        deposit.setAccountId(params.getAccountId());
        deposit.setCreateTime(params.getCreateTime());
        deposit.setModifyTime(params.getCreateTime());
        deposit.setFundSource(params.getFundSource());
        deposit.setDepositPostscript(CommonUtil.genRandom(6, 6));

        deposit.setDepositAmountCr(params.getFee());        // 存款金额 币
        deposit.setExchangeRate(params.getExchangeRate());  // 汇率
        deposit.setHash(params.getHash());
        deposit.setPayOrderNo(params.getHash());    // hash做第三方订单？
        agentDepositMapper.insert(deposit);
        return deposit;
    }


    public void dealEvelletTransferCallback(EvelletPayTransferCallbackDto data, String siteCode) {
        // 获得代付明细
        AgentMerchantDetail detail = withdrawMapper.findAgentMerchantDetailByTransId(data.getOutTradeno());
        if (null == detail) {
            log.info("agent evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==回调==订单明细不存在");
            return;
        }
        // 获得秘钥
        FundMerchantPay pay = merchantPayMapper.selectByPrimaryKey(detail.getMerchantId());
        if (null == detail) {
            log.info("agent evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==FundMerchantPay不存在");
            return;
        }
        // 验签
        Map<String, Object> param = jsonUtil.Entity2Map(data);
        param.remove("sign");
        param.remove("memo");
        String sign = MD5.getMD5(ASCIIUtils.getFormatUrl(param, pay.getMerchantKey()));
        if (!sign.equals(data.getSign())) {
            log.info("evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "签名错误");
            return;
        }
        // 获得取款
        Integer withDrawId = detail.getAccWithdrawId();
        AgyWithdraw withdraw = agyWithdrawMapper.selectByPrimaryKey(withDrawId);
        if (Constants.EVNumber.one == withdraw.getStatus() || Constants.EVNumber.zero == withdraw.getStatus()) {   // TODO
            log.info("agent evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==outTradeNo==" + data.getOutTradeno() + "==回调==已处理==status==" + withdraw.getStatus());
            return;
        }
        String redisKey = RedisConstants.UPDATE_WITHDRAW + siteCode + withdraw.getId();
        boolean flag = redisService.setRedisExpiredTimeBo(redisKey, data.getOutTradeno(), 200, TimeUnit.SECONDS);
        if (flag) {
            try {
                if (Integer.valueOf(Constants.EVNumber.two).equals(data.getStatus())) {           // 代付成功
                    log.info("agent evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==outTradeNo==" + data.getOutTradeno() + "==回调==代付成功");
                    withdrawService.updateMerchantPaymentStatus(Constants.EVNumber.one, withdraw.getId(), detail.getId(), "2", null, data.getHash());
                } else if (Integer.valueOf(Constants.EVNumber.three).equals(data.getStatus())) {   // 代付失败后，改为手动出款
                    log.info("agent evelletTransferCallback==siteCode==" + siteCode + "==loginName==" + data.getLoginName() + "==outTradeNo==" + data.getOutTradeno() + "==回调==代付失败");
                    if (StringUtils.isEmpty(data.getMemo())) {
                        data.setMemo("agentevellet代付失败！");
                    }
                    withdrawService.updateMerchantPaymentStatus(Constants.EVNumber.three, withdraw.getId(), detail.getId(),
                            "3", data.getMemo(), null);
                }
            } finally {
                redisService.del(redisKey);
            }
        }
    }
}
