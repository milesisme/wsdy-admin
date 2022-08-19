package com.wsdy.saasops.modules.fund.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.MerchantPayConstants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.pay.OnePayUtil;
import com.wsdy.saasops.modules.fund.dto.*;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Service
public class OnePayService {
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private JsonUtil jsonUtil;

    @Value("${panzi.callback.url}")
    private String onePayCallbackUrl;//所有回调地址是一个

    private static final String debit_payment = "pay.php"; // 代付接口

    private static final String debit_query = "payQuery.php"; // 查询接口

    /**
     * 发送转账数据
     * @param orderNo
     * @param bankcard
     * @param totalFee
     * @param merchantPay
     * @return
     */
    public OnePayResponseDto debitPayment(String orderNo, MbrBankcard bankcard,
                                          BigDecimal totalFee, FundMerchantPay merchantPay,String siteCode) {
        Integer bankSign = MerchantPayConstants.aoYoMerchantPayMap.get(bankcard.getBankName());
        if (Objects.isNull(bankSign)){
            OnePayResponseDto responseDto = new OnePayResponseDto();
            responseDto.setMsg("不支持该银行");
            return responseDto;
        }
        OnePayReqEncryptDto onePayReqEncryptDto = getReqEncryptDto(orderNo, bankcard, totalFee,merchantPay,siteCode);
        String webSite = merchantPay.getUrl();
        if(!webSite.endsWith("/")) {
            webSite+="/";
        }
        String url = webSite+ debit_payment;
        return sendToOnePayServer(onePayReqEncryptDto,url);
    }

    /**
     * 发送查询数据
     * @param orderNo
     * @param merchantPay
     * @return
     */
    public OnePayResponseDto querySubmitSuccess(String orderNo, FundMerchantPay merchantPay){
        OnePayReqBaseDto baseDto = getBaseDto(orderNo,merchantPay);
        String webSite = merchantPay.getUrl();
        if(!webSite.endsWith("/")) {
            webSite+="/";
        }
        String url = webSite+ debit_query;
        return sendToOnePayServer(baseDto,url);
    }

    private OnePayResponseDto sendToOnePayServer(OnePayReqBaseDto reqDto , String url){
        log.info("发送OnePay代付数据信息【" + JSON.toJSONString(reqDto) + "】");
        String result = okHttpService.postForm(okHttpService.getPayHttpsClient(), url, jsonUtil.toStringMap(reqDto));
        log.info("发送OnePay代付返回信息【" + result + "】");
        return jsonUtil.fromJson(result, OnePayResponseDto.class);
    }

    private OnePayReqEncryptDto getReqEncryptDto(String orderNo, MbrBankcard bankcard, BigDecimal totalFee, FundMerchantPay merchantPay,String siteCode) {

        OnePayReqEncryptDto onePayReqEncryptDto = new OnePayReqEncryptDto();
        onePayReqEncryptDto.setMer_id(merchantPay.getMerchantNo());
        onePayReqEncryptDto.setMer_ordersid(orderNo);
        onePayReqEncryptDto.setBank_code(MerchantPayConstants.aoYoMerchantPayMap.get(bankcard.getBankName()));
        onePayReqEncryptDto.setCard_name(bankcard.getRealName());
        onePayReqEncryptDto.setCard_num(bankcard.getCardNo());
        onePayReqEncryptDto.setMoney(CommonUtil.adjustScale(totalFee) + StringUtils.EMPTY);
        onePayReqEncryptDto.setNotify_url(onePayCallbackUrl+PayConstants.ONEPAY_PAYURL+siteCode);
        onePayReqEncryptDto.setTime_stamp(DateUtil.getCurrentDate("yyyyMMddHHmmss"));
        String signStr = OnePayUtil.getSign(onePayReqEncryptDto,merchantPay.getMerchantKey());
        log.info(onePayReqEncryptDto.getMer_ordersid()+"submitSign:"+signStr);
        onePayReqEncryptDto.setSignature(signStr);
        return onePayReqEncryptDto;
    }

    private OnePayReqBaseDto getBaseDto(String orderNo, FundMerchantPay merchantPay){

        OnePayReqBaseDto onePayReqBaseDto = new OnePayReqBaseDto();
        onePayReqBaseDto.setMer_ordersid(orderNo);
        onePayReqBaseDto.setMer_id(merchantPay.getMerchantNo());
        onePayReqBaseDto.setTime_stamp(DateUtil.getCurrentDate("yyyyMMddHHmmss"));
        String sign = OnePayUtil.getQuerySign(onePayReqBaseDto,merchantPay.getMerchantKey());
        log.info(onePayReqBaseDto.getMer_ordersid()+"querySign:"+sign);
        onePayReqBaseDto.setSignature(sign);
        return onePayReqBaseDto;
    }

}
