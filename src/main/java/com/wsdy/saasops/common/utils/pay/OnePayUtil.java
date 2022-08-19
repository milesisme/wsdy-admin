package com.wsdy.saasops.common.utils.pay;

import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.modules.fund.dto.OnePayReqEncryptDto;
import com.wsdy.saasops.modules.fund.dto.OnePayResponseDto;
import com.wsdy.saasops.modules.fund.dto.OnePayReqBaseDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OnePayUtil {

    private static String signPermsStr = "bank_code=#{bank_code}&card_name=#{card_name}&card_num=#{card_num}&mer_id=#{mer_id}&mer_ordersid=" +
            "#{mer_ordersid}&money=#{money}&notify_url=#{notify_url}&time_stamp=#{time_stamp}&key=#{key}";
    private static String querySignPermsStr = "mer_id=#{mer_id}&mer_ordersid=#{mer_ordersid}&time_stamp=#{time_stamp}&key=#{key}";
    private static String callbackPermsStr = "bank_code=#{bank_code}&card_name=#{card_name}&card_num=#{card_num}&mer_id=#{mer_id}&mer_ordersid=" +
            "#{mer_ordersid}&money=#{money}&status=#{status}&time_stamp=#{time_stamp}&key=#{key}";;

    public static String getSign(OnePayReqEncryptDto requestDto, String key){
        String noSignStr = signPermsStr.replace("#{mer_id}",requestDto.getMer_id())
                .replace("#{mer_ordersid}",requestDto.getMer_ordersid().toString())
                .replace("#{money}",requestDto.getMoney())
                .replace("#{card_num}",requestDto.getCard_num())
                .replace("#{card_name}",requestDto.getCard_name())
                .replace("#{bank_code}",requestDto.getBank_code().toString())
                .replace("#{notify_url}",requestDto.getNotify_url())
                .replace("#{time_stamp}",requestDto.getTime_stamp())
                .replace("#{key}",key);
        log.info("noSignStr:"+noSignStr);
        String signStr = MD5.getMD5(noSignStr);
        return signStr;
    }

    public static String getQuerySign(OnePayReqBaseDto requestDto, String key){
        String noSignStr = querySignPermsStr.replace("#{mer_id}",requestDto.getMer_id())
                .replace("#{mer_ordersid}",requestDto.getMer_ordersid())
                .replace("#{time_stamp}",requestDto.getTime_stamp())
                .replace("#{key}",key);
        log.info("noSignStr:"+noSignStr);
        String signStr = MD5.getMD5(noSignStr);
        return signStr;
    }

    public static String getCallbackSign(OnePayResponseDto requestDto, String key){
        String noSignStr = callbackPermsStr.replace("#{mer_id}",requestDto.getMer_id())
                .replace("#{mer_ordersid}",requestDto.getMer_ordersid().toString())
                .replace("#{money}",requestDto.getMoney())
                .replace("#{card_num}",requestDto.getCard_num())
                .replace("#{card_name}",requestDto.getCard_name())
                .replace("#{bank_code}",requestDto.getBank_code().toString())
                .replace("#{status}",requestDto.getStatus())
                .replace("#{time_stamp}",requestDto.getTime_stamp())
                .replace("#{key}",key);
        log.info("noSignStr:"+noSignStr);
        String signStr = MD5.getMD5(noSignStr);
        return signStr;
    }

    public static boolean checkSign(OnePayResponseDto responseDto, String key){
        String sign  = getCallbackSign(responseDto,key);
        return sign.equals(responseDto.getSignature());
    }

}
