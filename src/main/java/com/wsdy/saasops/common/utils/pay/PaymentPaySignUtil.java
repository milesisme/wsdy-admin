package com.wsdy.saasops.common.utils.pay;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PaymentPaySignUtil {


    public final static String CHARACTER_ENCODING_UTF_8 = "UTF-8";

    /**
     * RSA私钥加签
     * @param priKeyText 经过base64处理后的私钥
     * @param plainText 明文内容
     * @return 十六进制的签名字符串
     * @throws Exception
     */
    public static String sign(byte[] priKeyText, String plainText) throws Exception {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(priKeyText));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey prikey = keyf.generatePrivate(priPKCS8);

            // 用私钥对信息生成数字签名
            Signature signet = Signature.getInstance("SHA256withRSA");
            signet.initSign(prikey);
            signet.update(plainText.getBytes("UTF-8"));
            return PaymentPayDigestUtil.byte2hex(signet.sign());
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 公钥验签
     * @param pubKeyText 经过base64处理后的公钥
     * @param plainText 明文内容
     * @param signText 十六进制的签名字符串
     * @return 验签结果 true验证一致 false验证不一致
     */
    public static boolean verify(byte[] pubKeyText, String plainText, String signText) {
        try {
            // 解密由base64编码的公钥,并构造X509EncodedKeySpec对象
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(pubKeyText));
            // RSA算法
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            // 取公钥匙对象
            PublicKey pubKey = keyFactory.generatePublic(bobPubKeySpec);
            // 十六进制数字签名转为字节
            byte[] signed = PaymentPayDigestUtil.hex2byte(signText.getBytes("UTF-8"));
            Signature signatureChecker = Signature.getInstance("SHA256withRSA");
            signatureChecker.initVerify(pubKey);
            signatureChecker.update(plainText.getBytes("UTF-8"));
            // 验证签名是否正常
            return signatureChecker.verify(signed);
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 公钥加密
     * @param pubKeyText
     * @param plainText
     * @return
     * @throws Exception
     */
    public static String signPubKey(byte[] pubKeyText, String plainText) throws Exception {
        try {
            byte[] data = plainText.getBytes("UTF-8");
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(pubKeyText));
            PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            //分段加密
            byte[] enBytes = null;
            for (int i = 0; i < data.length; i += 128) {
                byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(data, i,i + 128));
                enBytes = ArrayUtils.addAll(enBytes, doFinal);
            }
            return PaymentPayDigestUtil.byte2hex(enBytes);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 私钥解密
     * @param privateText
     * @param plainText
     * @return
     */
    public static String privateDecrypt(byte[] privateText, String plainText) throws Exception {
        try {
            byte[] data = PaymentPayDigestUtil.hex2byte(plainText.getBytes("UTF-8"));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateText));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            //分段解密
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < data.length; i += 256) {
                byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(data, i, i + 256));
                sb.append(new String(doFinal));
            }
            return sb.toString();
        } catch (Exception e) {
            throw e;
        }
    }

    public static String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (!"data".equals(key) &&!"sign".equals(key) && notEmpty(params.get(key))) {
                sb.append(key).append("=").append(value).append("&");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static boolean notEmpty(String s) {
        return s != null && !"".equals(s) && !"null".equals(s);
    }
}