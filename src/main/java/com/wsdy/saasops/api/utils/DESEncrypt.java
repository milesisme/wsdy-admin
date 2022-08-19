package com.wsdy.saasops.api.utils;

import java.io.IOException;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DESEncrypt {
	String key;

	public DESEncrypt() {
	}

	public DESEncrypt(String key) {
		this.key = key;
	}
	public byte[] desEncrypt(byte[] plainText) throws Exception {
		SecureRandom sr = new SecureRandom();
		DESKeySpec dks = new DESKeySpec(key.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, key, sr);
        byte[] data = plainText;
        byte[] encryptedData = cipher.doFinal(data);
		return encryptedData;
	}

	public byte[] desDecrypt(byte[] encryptText) throws Exception {
		SecureRandom sr = new SecureRandom();
		DESKeySpec dks = new DESKeySpec(key.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

		SecretKey key = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.DECRYPT_MODE, key, sr);
        byte[] encryptedData = encryptText;
        byte[] decryptedData = cipher.doFinal(encryptedData);
		return decryptedData;
	}

	public String encrypt(String input) throws Exception {
		return base64Encode(desEncrypt(input.getBytes())).replaceAll("\\s*", "");
	}

	public String decrypt(String input) throws Exception {
		byte[] result = base64Decode(input);
		return new String(desDecrypt(result));
	}

	public String base64Encode(byte[] s) {
		return Base64.getEncoder().encodeToString(s);
	}

	public byte[] base64Decode(String s) throws IOException {
		return Base64.getDecoder().decode(s);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

/*	public static void main(String args[]) {
		try {
			DESEncrypt d = new DESEncrypt("J5g6PorI");
			//String p = d.encrypt("cagent=L77_AGIN/\\\\/loginname=vebdanny006/\\\\/method=lg/\\\\/actype=1/\\\\/password=xy43vi/\\\\/oddtype=a/\\\\/cur=CNY");
			//System.out.println("密文:" + p);
			//String m=d.decrypt("0yNt8MZx9q6VX9MqLPMUJZumxfKAvrtqhkF0ndoZu5VprVKUdTdRPBpFYe8GSK/9N7fxvY1/PKomi583NVOPEAON/Wo/ZB4KLmykrg5v7GHl+sXDFJo+OCjeZwM7HQFclZlHiSs+ukkKNkQk5okRDg==");
		System.out.println(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
}
