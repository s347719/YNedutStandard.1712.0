package com.yineng.ynmessager.util;

import android.util.Base64;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DESUtil {

	public static final String KEY = "nedutkey";
	public static final String VI = "nedutkey";
	private static Cipher enCipher;
	private static Cipher deCipher;

	static {

		try {
			byte[] keyByte = KEY.getBytes();
			byte[] viByte = VI.getBytes();
			// 创建md5散列对象
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 散列密钥
			md.update(keyByte);
			// 获得des密钥
			DESKeySpec desKey = new DESKeySpec(keyByte);
			// 获得des加密密钥工厂
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
			// 获得加密后的des密钥
			SecretKey seKey = skf.generateSecret(desKey);
			// 获得初始化向量对象
			IvParameterSpec ivP = new IvParameterSpec(viByte);
			AlgorithmParameterSpec paramSpec = ivP;
			// 为加密算法指定填充方式，创建加密会话对象
			enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			// 初始化加密回话对象
			enCipher.init(Cipher.ENCRYPT_MODE, seKey, paramSpec);
			deCipher.init(Cipher.DECRYPT_MODE, seKey, paramSpec);

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 *            mes 要加密的信息
	 * @return String 返回加密后的信息
	 */
	public static String encrypt(String mes) {
		if (mes == null || "".equals(mes)) {
			return null;
		}

		byte[] data = mes.getBytes();
		byte[] enc = null;

		synchronized (enCipher) {
			try {
				enc = enCipher.doFinal(data);
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			}
		}

		if (enc != null) {

			return Base64.encodeToString(enc, Base64.DEFAULT);
		} else {
			return null;
		}
	}

	/**
	 *            mes 加密后的信息
	 * @return String 返回解密后的信息
	 */
	public static String decrypt(String mes) throws IOException {
		if (mes == null || "".equals(mes)) {
			return null;
		}

		byte[] des = Base64.decode(mes, Base64.DEFAULT);
		byte[] enc = null;

		synchronized (deCipher) {
			try {
				enc = deCipher.doFinal(des);
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			}
		}

		if (enc != null) {
			return new String(enc);
		} else {
			return null;
		}
	}

	
	private static byte[] convertHexString(String ss) {
		byte digest[] = new byte[ss.length() / 2];
		for (int i = 0; i < digest.length; i++) {
			String byteString = ss.substring(2 * i, 2 * i + 2);
			int byteValue = Integer.parseInt(byteString, 16);
			digest[i] = (byte) byteValue;
		}
		return digest;
	}

	private static String toHexString(byte b[]) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			String plainText = Integer.toHexString(0xff & b[i]);
			if (plainText.length() < 2) {
                plainText = "0" + plainText;
            }
			hexString.append(plainText);
		}
		return hexString.toString();
	}

	// 解密
	public static String decrypt(String message, String key) throws Exception {
		byte[] bytesrc = convertHexString(message);
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
		IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
		cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
		byte[] retByte = cipher.doFinal(bytesrc);
		return new String(retByte, "utf-8");
	}

	// 加密
	public static String encrypt(String value, String key) {
		String result = "";
		try {
			// value = java.net.URLEncoder.encode(value, "utf-8");
			result = toHexString(encryptToByte(value, key)).toUpperCase();
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
		return result;
	}

	private static byte[] encryptToByte(String message, String key)
			throws Exception {
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
		IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		return cipher.doFinal(message.getBytes("UTF-8"));
	}
}
