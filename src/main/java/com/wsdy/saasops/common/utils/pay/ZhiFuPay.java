package com.wsdy.saasops.common.utils.pay;

import java.util.*;

public class ZhiFuPay {

    public static String getSign(Map<String, String> params, String key) {
        return createSign(params, key);
    }

    public static String createSign(Map<String, String> map, String key) {
        map.remove("sign");
        map = Utils.sortMapByKey(map);
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                sb.append(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        String signStr = sb.append("secret=" + key).toString();
        return MD5Encoder.encode(signStr);
    }

}
