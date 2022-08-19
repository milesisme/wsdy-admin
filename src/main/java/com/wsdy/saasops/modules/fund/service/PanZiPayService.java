package com.wsdy.saasops.modules.fund.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;

import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.modules.fund.dao.AccWithdrawMapper;
import com.wsdy.saasops.modules.fund.dto.*;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.pay.PZSignUtils;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PanZiPayService {

    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private AccWithdrawMapper accWithdrawMapper;

    private static final String debit_payment = "debit/payment.do"; // 代付请求
    private static final String debit_query = "debit/query.do";// 代付查询

    public PZQueryResponseDto debitPayment(String orderNo, String bankCode, MbrBankcard bankcard,
                                            BigDecimal totalFee, FundMerchantPay merchantPay) {
        PZPaymentRequestDto requestDto = new PZPaymentRequestDto();
        requestDto.setPartner_id(merchantPay.getMerchantNo());
        requestDto.setBank_id(merchantPay.getBankId());
        requestDto.setBank_code(bankCode);
        requestDto.setCard_no(bankcard.getCardNo());
        requestDto.setCard_type(Constants.EVNumber.one + StringUtils.EMPTY);
        requestDto.setCard_prop(Constants.EVNumber.one + StringUtils.EMPTY);
        requestDto.setOut_trade_no(orderNo);
        requestDto
                .setTotal_fee(totalFee.multiply(new BigDecimal(Constants.ONE_HUNDRED)).intValue() + StringUtils.EMPTY);
        requestDto.setNotify_url("No");
        String sign = PZSignUtils.buildRequestSign(jsonUtil.toStringMap(requestDto), merchantPay.getMerchantKey());
        requestDto.setSign(sign);
        requestDto.setCard_name(bankcard.getRealName());
        requestDto.setCard_province(bankcard.getProvince());
        requestDto.setCard_city(bankcard.getCity());
        requestDto.setCard_branch(bankcard.getAddress());
        String url2 = merchantPay.getUrl() + debit_payment;
        String result = okHttpService.postJson(okHttpService.getPayHttpsClient(), url2, requestDto,null);
        log.info("盘子代付请求返回数据信息【" + result + "】");
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        return jsonUtil.fromJson(result, PZQueryResponseDto.class);
    }

    public PZQueryContentDto debitQuery(AccWithdraw accWithdraw, FundMerchantPay merchantPay) {
        if (isNull(merchantPay)) {
            return null;
        }
        PZQueryRequestDto requestDto = new PZQueryRequestDto();
        requestDto.setPartner_id(merchantPay.getMerchantNo());
        requestDto.setOut_trade_no(accWithdraw.getTransId());
        String sign = PZSignUtils.buildRequestSign(jsonUtil.toStringMap(requestDto), merchantPay.getMerchantKey());
        requestDto.setSign(sign);
        String url = merchantPay.getUrl() + debit_query;
        String result = okHttpService.postJson(okHttpService.getPayHttpsClient(), url, requestDto,null);
        if (nonNull(result)) {
            PZQueryResponseDto responseDto = jsonUtil.fromJson(result, PZQueryResponseDto.class);
            if (Boolean.FALSE.equals(responseDto.getSuccess())) {
                accWithdraw.setMemo(responseDto.getMessage());
                accWithdrawMapper.updateByPrimaryKey(accWithdraw);
            }
            log.info("盘子代付请求返回数据信息【" + result + "】");
            if (Boolean.TRUE.equals(responseDto.getSuccess())) {
                return jsonUtil.fromJson(JSON.toJSONString(responseDto.getContent()), PZQueryContentDto.class);
            }
        }
        return null;
    }
}
