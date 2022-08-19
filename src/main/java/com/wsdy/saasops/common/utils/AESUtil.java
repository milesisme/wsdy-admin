package com.wsdy.saasops.common.utils;

import com.wsdy.saasops.common.exception.RRException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;


@Component
@Slf4j
public class AESUtil {
    public static void main(String[] args) throws Exception {
        String s = encrypt("tyw");
        System.out.println(s);
        String c = decrypt("399e37c0c4526aa76325ee6d3071a737191230T130857969");
        System.out.println(c);
    }

    /**
     * AES加密
     *
     * @param content
     * @return
     * @throws Exception
     */
    public static String encrypt(String content) throws Exception {
        String encryptKey = genHexKey();
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));
        byte[] encryptBytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(encryptBytes) + encryptKey;
    }

    public static String decrypt(String content) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            String key = content.substring(content.length() - 16);
            String encstr = content.substring(0, content.length() - 16);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"));
            byte[] decryptBytes = cipher.doFinal(Hex.decodeHex(encstr.toCharArray()));
            return new String(decryptBytes);

        } catch (Exception e) {
            log.info(e.getMessage());
            log.error("schemaName error");
            throw new RRException("前端请求，SToken错误，" + content);
        }
    }

    private static String genHexKey() {
        int length = 16;
        String str = "0123456789abcdef";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(str.length());
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    private static String genTimeKey() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyMMdd'T'HHmmssSSS");
        String key = timeFormatter.format(LocalDateTime.now());
        return key;
    }
}
