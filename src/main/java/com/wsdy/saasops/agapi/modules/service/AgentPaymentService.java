package com.wsdy.saasops.agapi.modules.service;

import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.pay.dto.PayParams;
import com.wsdy.saasops.api.modules.pay.dto.PayResponseDto;
import com.wsdy.saasops.api.modules.pay.dto.saaspay.CommonPayQueryResp;
import com.wsdy.saasops.api.modules.pay.dto.saaspay.PaySearchResponseDto;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.ASCIIUtils;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.api.utils.OkHttpUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgentDepositMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentDeposit;
import com.wsdy.saasops.modules.agent.entity.AgyBillDetail;
import com.wsdy.saasops.modules.agent.entity.AgyWallet;
import com.wsdy.saasops.modules.agent.mapper.DepositMapper;
import com.wsdy.saasops.modules.agent.service.AgentWalletService;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicOnlinepay;
import com.wsdy.saasops.modules.system.pay.entity.SysDeposit;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.common.constants.Constants.ONE_HUNDRED;
import static com.wsdy.saasops.common.constants.Constants.SYSTEM_USER;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
public class AgentPaymentService {

    @Autowired
    private AgentDepositMapper agentDepositMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private DepositMapper xmldepositMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private AgentWalletService walletService;
    @Autowired
    private AgentAccountMapper agentAccountMapper;

    public void payCallback(String orderNo, String siteCode) {
        AgentDeposit fundDeposit = new AgentDeposit();
        fundDeposit.setOrderNo(orderNo);
        fundDeposit.setStatus(Constants.EVNumber.two);
        AgentDeposit deposit = agentDepositMapper.selectOne(fundDeposit);
        if (nonNull(deposit)) {
            getPayResult(deposit, siteCode);
        }
    }

    @Async("getPayResultExecutor")
    public void getPayResult(AgentDeposit deposit, String siteCode) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        if (deposit.getMark() == Constants.EVNumber.zero || StringUtils.isNotEmpty(deposit.getPayOrderNo())) {
            String key = RedisConstants.QUERY_AGENT_PAY + siteCode + deposit.getId();
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, deposit.getId(), 200, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isExpired)) {
                try {
                    AgentDeposit fundDeposit = agentDepositMapper.selectByPrimaryKey(deposit.getId());
                    if (fundDeposit.getStatus() == Constants.EVNumber.two) {
                        if (StringUtils.isNotEmpty(fundDeposit.getPayOrderNo())) {
                            updateDepositFastpay(fundDeposit, siteCode);
                        } else {
                            updateDepositOnlinepay(fundDeposit, siteCode);
                        }
                    }
                } finally {
                    redisService.del(key);
                }
            }
        }
    }

    private void updateDepositOnlinepay(AgentDeposit fundDeposit, String siteCode) {
        if (nonNull(fundDeposit.getOnlinePayId())) {
            SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(fundDeposit.getOnlinePayId());
            if (nonNull(onlinepay)) {
                setFundDeposit(fundDeposit);
                if (PayConstants.SAASOPS_PAY_CODE.equals(onlinepay.getPlatfromCode())) {
                    updateSaasopsPay(onlinepay.getMerNo(), onlinepay.getPassword(), onlinepay.getPayUrl(), siteCode, fundDeposit);
                }
            }
        }
    }

    private void updateDepositFastpay(AgentDeposit deposit, String siteCode) {
        if (nonNull(deposit.getCompanyPayId())) {
            SysDeposit sysDeposit = payMapper.findSysDepositById(deposit.getCompanyPayId());
            setFundDeposit(deposit);
            String platFormCode = payMapper.findPayId(sysDeposit.getFastPayId());
            if (PayConstants.SAASOPS_PAY_CODE.equals(platFormCode)) {
                updateSaasopsPay(sysDeposit.getCid(), sysDeposit.getPassword(), sysDeposit.getPayUrl(), siteCode, deposit);
                return;
            }
            // 此处不会再更新(success状态下)
            xmldepositMapper.updatePayStatus(deposit);
        }
    }

    public void updateSaasopsPay(String merchantNo, String merchantKey, String url, String siteCode, AgentDeposit deposit) {
        Map<String, Object> paramsMap = new HashMap<>(4);
        paramsMap.put("outTradeNo", deposit.getOrderNo());
        paramsMap.put("merchantNo", merchantNo);
        String urlParams = ASCIIUtils.getFormatUrl(paramsMap, merchantKey);
        String sign = MD5.getMD5(urlParams);
        paramsMap.put("sign", sign);

        log.info("'outTradeNo==" + deposit.getOrderNo() + "==查询==请求参数==" + jsonUtil.toJson(paramsMap));
        // 处理url
        String payUrl = url + PayConstants.SAASOPS_PAY_QUERY;
        if (!url.endsWith("/")) {
            payUrl = url + "/" + PayConstants.SAASOPS_PAY_QUERY;
        }

        String result = OkHttpUtils.postForm(payUrl, jsonUtil.toStringMap(paramsMap));
        log.info("'outTradeNo==" + deposit.getOrderNo() + "==查询==返回参数==" + result);
        if (StringUtils.isEmpty(result)) {
            return;
        }
        CommonPayQueryResp response = jsonUtil.fromJson(result, CommonPayQueryResp.class);
        if (isNull(response) || response.getCode() != 200 || isNull(response.getData())) {
            return;
        }
        PaySearchResponseDto searchResponseDto = response.getData();
        if (nonNull(searchResponseDto)) {
            if (Constants.EVNumber.one == searchResponseDto.getStatus()) {
                boolean isActualArrival = false;
                if (StringUtils.isEmpty(searchResponseDto.getMemo())) {
                    deposit.setMemo("支付成功，网关支付");
                } else {
                    deposit.setMemo(searchResponseDto.getMemo());
                }
                // 修改实际入款金额
                if (Objects.nonNull(searchResponseDto.getActualarrival()) && (searchResponseDto.getActualarrival().compareTo(new BigDecimal(0)) > 0)) {
                    deposit.setActualArrival(searchResponseDto.getActualarrival());
                    isActualArrival = true;
                }
                // 支付成功处理
                updateDepositSucceed(deposit, true, isActualArrival);
            } else if (Constants.EVNumber.zero == searchResponseDto.getStatus()) {
                if (StringUtils.isEmpty(searchResponseDto.getMemo())) {
                    deposit.setMemo("支付失败，网关支付");
                } else {
                    deposit.setMemo(searchResponseDto.getMemo());
                }
                deposit.setStatus(Constants.EVNumber.zero);
            } else {
                return;
            }
            // 只更新查过12小时的订单
            xmldepositMapper.updatePayStatus(deposit);
        }
    }

    public void updateDepositSucceed(AgentDeposit fundDeposit, boolean isUpdateStatus, boolean isActualArrival) {
        // 先更新状态，避免并发导致的多次上分
        fundDeposit.setStatus(Constants.IsStatus.succeed);
        fundDeposit.setAuditUser(SYSTEM_USER);
        fundDeposit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundDeposit.setIsPayment(Boolean.TRUE);
        int i = 0;
        if (isUpdateStatus) {
            i = xmldepositMapper.updatePayStatus(fundDeposit);
        }
        if (i > 0 || !isUpdateStatus) {

            AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(fundDeposit.getAccountId());
            // 上分
            AgyWallet wallet = walletService.setAgyWallet(agentAccount,
                    fundDeposit.getActualArrival(),
                    OrderConstants.AGENT_ORDER_CODE_ACK, null, null,
                    fundDeposit.getOrderNo(), agentAccount.getAgyAccount(),
                    Constants.EVNumber.one);
            AgyBillDetail billDetail =
                    walletService.addWalletAndBillDetail(
                            wallet, Constants.EVNumber.one);

            // 此处单独更新billid
            fundDeposit.setBillDetailId(billDetail.getId());
            if (isUpdateStatus) {
                AgentDeposit depositBill = new AgentDeposit();
                depositBill.setId(fundDeposit.getId());
                depositBill.setBillDetailId(billDetail.getId());
                agentDepositMapper.updateByPrimaryKeySelective(depositBill);
            }
            // 额度变化：使用存款金额，不用实际金额
            if (Objects.nonNull(fundDeposit.getCompanyPayId())) {
                payMapper.updateDepositAmount(fundDeposit.getDepositAmount(), fundDeposit.getCompanyPayId());
            }
            if (Objects.nonNull(fundDeposit.getOnlinePayId())) {
                payMapper.updateOnlinePayAmount(fundDeposit.getDepositAmount(), fundDeposit.getOnlinePayId());
            }
            if (Objects.nonNull(fundDeposit.getQrCodeId())) {
                payMapper.updateQrCodeAmount(fundDeposit.getDepositAmount(), fundDeposit.getQrCodeId());
            }
        }
    }

    public void setFundDeposit(AgentDeposit fundDeposit) {
        fundDeposit.setModifyUser(SYSTEM_USER);
        fundDeposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundDeposit.setAuditUser(SYSTEM_USER);
        fundDeposit.setAuditTime(fundDeposit.getModifyTime());
        fundDeposit.setStatus(Constants.EVNumber.zero);
        fundDeposit.setIsPayment(Boolean.FALSE);
    }
}
