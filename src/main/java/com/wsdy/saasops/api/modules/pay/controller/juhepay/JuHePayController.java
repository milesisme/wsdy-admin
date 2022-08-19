package com.wsdy.saasops.api.modules.pay.controller.juhepay;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.pay.dto.PayParams;
import com.wsdy.saasops.api.modules.pay.dto.saaspay.PayTradeRequestDto;
import com.wsdy.saasops.api.modules.pay.dto.uxiangpay.JuFuPayCreateOderReqDto;
import com.wsdy.saasops.api.modules.pay.service.PayInfoService;
import com.wsdy.saasops.api.modules.pay.service.PaymentService;
import com.wsdy.saasops.api.utils.ASCIIUtils;
import com.wsdy.saasops.api.utils.HttpsRequestUtil;
import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.fund.dao.FundDepositMapper;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.fund.service.FundDepositService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrDepositCountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrDepositCond;
import com.wsdy.saasops.modules.member.entity.MbrDepositCount;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrDepositCondService;
import com.wsdy.saasops.modules.member.service.MbrDepositLockLogService;
import com.wsdy.saasops.modules.member.service.MbrVerifyService;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicOnlinepay;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@RestController
@RequestMapping("/api/juhepay")
@Api(value = "JuHePayController", tags = "聚合支付创建订单接口")
public class JuHePayController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private FundDepositService fundDepositService;
    @Autowired
    private PayInfoService payInfoService;
    @Autowired
    private MbrAccountService mbrAccountService;

    @Autowired
    private MbrAccountMapper mbrAccountMapper;

    @Autowired
    private MbrDepositCondService mbrDepositCondService;

    @Autowired
    private FundMapper fundMapper;

    @Autowired
    private PayMapper payMapper;

    @Autowired
    private MbrDepositLockLogService mbrDepositLockLogService;

    @Autowired
    private MbrDepositCountMapper mbrDepositCountMapper;

    @Autowired
    private FundDepositMapper fundDepositMapper;

    @Autowired
    private MbrVerifyService verifyService;

    @Value("${panzi.callback.url}")
    private String panziCallbackUrl;

    @Autowired
    private TCpSiteService tCpSiteService;


    @PostMapping("createOrder")
    public R createOrder(@RequestBody JuFuPayCreateOderReqDto dto, HttpServletRequest request) {

        log.info("==JuHePayController#createOrder==优享支付==创建订单==参数：{}", JSON.toJSONString(dto));

        if (StringUtils.isBlank(dto.getSiteCode())) {
            log.error("==JuHePayController#createOrder==优享支付==创建订单==siteCOde为空");
            return R.error(-2, "获取会员信息失败");
        }

        PayParams params = new PayParams();
        params.setOnlinePayId(dto.getOnlinePayId());
        params.setFee(new BigDecimal(dto.getFee()));
        params.setAccountId(dto.getAccountId());
        params.setSiteCode(dto.getSiteCode());
        params.setTerminal(dto.getTerminal());

        params.setFundSource(HttpsRequestUtil.getHeaderOfDev(request.getHeader("dev")));
        params.setOutTradeNo(new SnowFlake().nextId());
        params.setIp(CommonUtil.getIpAddress(request));

        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(dto.getSiteCode()));

        // 校验入款配置等
        try {
            payInfoService.checkoutOnlinePay(params);
        }catch (Exception e){
            log.error("==JuHePayController#createOrder==优享支付==创建订单==校验入款配置异常", e);
            return R.error(-3, "校验入款配置异常");
        }
        log.info("==JuHePayController#createOrder==优享支付==创建订单==校验入款配置正常");

        Integer accountId = params.getAccountId();
        // 获得会员
        MbrAccount account = mbrAccountMapper.selectByPrimaryKey(accountId);
        if (account == null) {
            log.error("==JuHePayController#createOrder==优享支付==创建订单==获取会员信息失败==用户id：{}", accountId);
            return R.error(-4, "获取会员信息失败");
        }

        log.info("==JuHePayController#createOrder==优享支付==创建订单==获取会员信息成功");

        // 校验存款计数
        MbrDepositCount count = null;
        try {
            count = payInfoService.checkDepositCount(account, Constants.EVNumber.zero);
        }catch (Exception e){
            log.error("==JuHePayController#createOrder==优享支付==创建订单==校验存款计数异常", e);
            return R.error(-5, "校验存款计数异常");
        }
        log.info("==JuHePayController#createOrder==优享支付==创建订单==校验存款计数正常");

        // 构建deposit对象
        FundDeposit deposit = buildFundDespoit(params);
        // 获取支付渠道信息
        SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(params.getOnlinePayId());
        // 效验存款锁定规则，是否应该锁定会员
        boolean lock = mbrDepositLockLogService.onlinepayUnpayLock(account, onlinepay.getPayId());
        if (lock) {
            log.info("==JuHePayController#createOrder==优享支付==创建订单==该用户已经被限制存款");
            return R.error(-6, "该用户已经被限制存款");
        }
        log.info("==JuHePayController#createOrder==优享支付==创建订单==该用户未被限制存款");

        // 构建返回值
        PayTradeRequestDto requestDto = null;
        if (PayConstants.JUHE_PAY_CODE.equals(onlinepay.getCode())) {
            // 提单
            if(StringUtil.isEmpty(params.getUserName())){
                params.setUserName(deposit.getDepositUser());
            }

            // 对照正常下单的 - commonPay
            requestDto = getCommonPayParam(params, onlinepay);
            // 保存订单
            if (nonNull(deposit)) {
                log.info("==JuHePayController#createOrder==优享支付==创建订单==保存充值记录到数据库");
                fundDepositMapper.insert(deposit);
                verifyService.addMbrVerifyDeposit(deposit, params.getSiteCode());
            }


            // 不存在成功入款单，则更新计数
            mbrDepositCountMapper.updateCount(count);
            // 更新会员存款锁定状态
            if (count.getIsUpdateDepositLock()) {
                log.info("==JuHePayController#createOrder==优享支付==创建订单==更新会员信息");
                MbrAccount mbr = new MbrAccount();
                mbr.setId(params.getAccountId());
                mbr.setDepositLock(Constants.EVNumber.one);
                mbrAccountMapper.updateByPrimaryKeySelective(mbr);
            }
        }

        return R.ok(200,"success").put("reqDto", JSON.toJSONString(requestDto));
    }

    private PayTradeRequestDto getCommonPayParam(PayParams params, SetBacicOnlinepay onlinepay) {
        PayTradeRequestDto requestDto = new PayTradeRequestDto();
        requestDto.setAmount(params.getFee());
        if (StringUtil.isNotEmpty(params.getBankCode()) && onlinepay.getPaymentType() == 4) {//网银
            requestDto.setBankCode(params.getBankCode());
        }
        requestDto.setEvbBankId(onlinepay.getBankId());
        // 处理url
        String callbackUrl = panziCallbackUrl + PayConstants.SAASOPS_PAY_NOTIFY_URL;
        if (!panziCallbackUrl.endsWith("/")) {
            callbackUrl = panziCallbackUrl + "/" + PayConstants.SAASOPS_PAY_NOTIFY_URL;
        }
        requestDto.setCallbackUrl(callbackUrl);
        requestDto.setIp(params.getIp());
        requestDto.setOutTradeNo(params.getOutTradeNo().toString());
        requestDto.setMerchantNo(onlinepay.getMerNo());
        requestDto.setReturnParams(params.getSiteCode());
        requestDto.setAccountId(params.getAccountId());     // 会员ID
        requestDto.setLoginName(params.getLoginName());     // 会员名
        requestDto.setTerminal(params.getFundSource());     // 支付终端
        requestDto.setPayUserName(params.getUserName());    // 支付者 银行卡名
        // 签名
        Map<String, Object> param = JSON.parseObject(JSON.toJSONString(requestDto), LinkedHashMap.class);
        param.remove("sign");
        param.remove("returnParams");
        String str = ASCIIUtils.getFormatUrl(param, onlinepay.getPassword());
        String sign = MD5.getMD5(str);
        requestDto.setSign(sign);
        return requestDto;
    }

    private FundDeposit buildFundDespoit(PayParams params) {
        FundDeposit deposit = new FundDeposit();
        deposit.setOrderNo(params.getOutTradeNo().toString());
        deposit.setMark(FundDeposit.Mark.onlinePay);
        deposit.setStatus(FundDeposit.Status.apply);
        deposit.setIsPayment(Boolean.FALSE);
        deposit.setOnlinePayId(params.getOnlinePayId());
        deposit.setDepositAmount(params.getFee());
        BigDecimal feeScale = getFeeScale(params.getFee(), params.getAccountId());
        deposit.setHandlingCharge(feeScale);
        deposit.setActualArrival(deposit.getDepositAmount().subtract(feeScale));
        deposit.setHandingback(Constants.Available.enable);
        deposit.setIp(params.getIp());
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_ONLINEDEPOSIT);
        MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(params.getAccountId());
        params.setLoginName(mbrAccount.getLoginName()); // 增加会员名
        deposit.setDepositUser(mbrAccount.getRealName());
        deposit.setCreateUser(mbrAccount.getLoginName());
        deposit.setAccountId(params.getAccountId());
        deposit.setCreateTime(DateUtil.format(new Date(), DateUtil.FORMAT_25_DATE_TIME));
        deposit.setModifyTime(DateUtil.format(new Date(), DateUtil.FORMAT_25_DATE_TIME));
        deposit.setFundSource(params.getFundSource());
        deposit.setSpreadCode(params.getSpreadCode());
        return deposit;
    }

    public BigDecimal getFeeScale(BigDecimal fee, Integer accountId) {
        // 根据该会员组,会员的存款设置,获取线上支付玩家单笔存款手续费及相关设置信息
        MbrDepositCond mbrDeposit = mbrDepositCondService.getMbrDeposit(accountId);
        if (isNull(mbrDeposit) || isNull(mbrDeposit.getFeeAvailable()) || mbrDeposit.getFeeAvailable() != 1) {
            return BigDecimal.ZERO;
        }
        // 判断限免,从支付流水中取出该用户支付数据,
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setAccountId(accountId);
        // 根据限免周期获得start时间
        String startTime = getStartTime(mbrDeposit.getFeeHours());
        // 实际这段时间的充值次数
        Map<String, Object> mbrFreeTimes = fundMapper.querySumFeeFreeTimes(accountId, startTime);
        BigDecimal freeTime = mbrFreeTimes != null ? (BigDecimal) mbrFreeTimes.get("freeTimes") : new BigDecimal(0);
        if (freeTime.compareTo(new BigDecimal(mbrDeposit.getFeeTimes())) == -1) {
            return BigDecimal.ZERO;
        }
        BigDecimal feeScale = fee.multiply(mbrDeposit.getFeeScale().divide(new BigDecimal(100))); // 手续费 按比例收费
        return feeScale.compareTo(mbrDeposit.getFeeTop()) == 1 ? mbrDeposit.getFeeTop() : feeScale; // 手续费
    }

    // 根据日/周/月获取时间：1日 2周 3月
    private String getStartTime(Integer rule) {
        String startTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
        if (Constants.EVNumber.one == rule) {
            startTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
        } else if (Constants.EVNumber.two == rule) {
            startTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//本周第一天
        } else if (Constants.EVNumber.three == rule) {
            startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//本月第一天
        }
        return startTime;
    }
}
