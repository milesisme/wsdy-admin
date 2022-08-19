package com.wsdy.saasops.common.utils.pay;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;

public class PZSignUtils {

    private static final String CHARSET = "UTF-8";

    /**
     * 生成签名结果
     *
     * @param params 要签名的数组
     * @return 签名结果字符串
     */
    public static String buildRequestSign(Map<String, String> params, String key) {
        // 除去数组中的空值和签名参数
        Map<String, String> sPara = paraFilter(params);
        String prestr = createLinkString(sPara);
        prestr = prestr + "&key=" + key;
        return DigestUtils.md5Hex(getContentBytes(prestr, CHARSET));
    }

    /**
     * 生成要请求给支付宝的参数数组
     *
     * @param sPara 请求前的参数数组
     * @return 要请求的参数数组
     */
    public static Map<String, String> buildRequestPara(Map<String, String> sPara, String key) {
        // 生成签名结果
        String mysign = buildRequestSign(sPara, key);
        // 签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        return sPara;
    }


    /**
     * 签名字符串
     *
     * @param text 需要签名的字符串
     * @param sign 签名结果
     *             密钥
     *             编码格式
     * @return 签名结果
     */
    public static boolean verify(String text, String sign, String key) {
        text = text + "&key=" + key;
        String mysign = DigestUtils.md5Hex(getContentBytes(text, CHARSET));
        if (mysign.equals(sign)) {
            return true;
        }
        return false;
    }

    /**
     * 验证响应参数
     *
     * @param params
     * @param sign
     * @return
     */
    public static boolean verifyReponse(Map<String, String> params, String sign, String key) {
        // 过滤空值、sign与sign_type参数
        Map<String, String> sParaNew = paraFilter(params);
        // 获取待签名字符串
        String preSignStr = createLinkString(sParaNew);
        // 获得签名验证结果
        return verify(preSignStr, sign, key);
    }

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

    /**
     * 除去数组中的空值，以及不需要进行签名的参数。过滤sign,sign_type,subject,extra,error
     *
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    private static Map<String, String> paraFilter(Map<String, String> sArray) {
        // key按字母顺序排序
        Map<String, String> result = new TreeMap<String, String>();
        if (sArray == null || sArray.size() <= 0) {
            return result;
        }
        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || "".equals(value) || "sign".equalsIgnoreCase(key)
                    || "sign_type".equalsIgnoreCase(key)
                    || "subject".equalsIgnoreCase(key) || "extra".equalsIgnoreCase(key)
                    || "error".equalsIgnoreCase(key)) {
                continue;
            }
            result.put(key, value);
        }
        return result;
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     *
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    private static String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder  prestr = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            // 拼接时，不包括最后一个&字符
            if (i == keys.size() - 1) {
                prestr.append(key).append("=").append(value);
            } else {
                prestr.append(key).append("=").append("&");
            }
        }
        return prestr.toString();
    }

}