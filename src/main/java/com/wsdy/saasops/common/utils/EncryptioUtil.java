package com.wsdy.saasops.common.utils;

import com.wsdy.saasops.common.utils.google.GoogleAuthenticatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
// import sun.misc.BASE64Decoder;
// import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

@Component
@Slf4j
public class EncryptioUtil {
    private final static String DES = "DES";
    private final static String ENCODE = "GBK";

    /**
     * Description 根据键值进行加密
     *
     * @param data 待加密数据
     * @param key  密钥
     * @return
     * @throws Exception
     */
    public static String encrypt(String data, String key) {
        try {
            byte[] bt = encrypt(data.getBytes(ENCODE), key.getBytes(ENCODE));
            // String strs = new BASE64Encoder().encode(bt);
            Encoder encoder = Base64.getEncoder();
            String strs = encoder.encodeToString(bt);
            return strs;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 根据键值进行解密
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String data, String key) {
        try {
            if (data == null)
                return null;
            // BASE64Decoder decoder = new BASE64Decoder();
            Decoder decoder = Base64.getDecoder();
            byte[] buf = decoder.decode(data);
            byte[] bt = decrypt(buf, key.getBytes(ENCODE));
            return new String(bt, ENCODE);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Description 根据键值进行加密
     *
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);
        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(DES);
        // 用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }

    /**
     * Description 根据键值进行解密
     *
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);
        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(DES);
        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }

    public static void main(String[] args) {
        String data = "18778898999";
        String key = GoogleAuthenticatorUtils.createSecretKey(); // 秘钥
        System.out.println(GoogleAuthenticatorUtils.createSecretKey());
        System.out.println("加密前===>" + data);
        try {
            String jiami = encrypt(data, key);
            System.err.println("加密后：" + jiami);
            String jiemi = decrypt("6cza7VVqPN2u0M3j8qn/BA==", "gabjspdebalxl7b7htbsp64m4olrjdwr");
            System.err.println("解密后：" + jiemi);

        } catch (Exception e) {
            System.out.println("秘钥不正确" + "或" + "加密后的码被更改");
            e.printStackTrace();
        }
    }

}
