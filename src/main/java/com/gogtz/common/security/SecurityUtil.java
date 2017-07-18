package com.gogtz.common.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * 加密解密共通方法
 *
 * @version 0.01
 * <p>改版履历
 * <ul>
 * <li>2014/09/19  Ver.0.01  wh.tian  Create
 * </ul>
 * </p>
 */
public class SecurityUtil {
	/**
	 * 加密
	 *
	 * @param keyStr
	 * @param val
	 * @return
	 */
	public static String entry(String keyStr, String val) {
		String p1 = "";
		Key key = getKey(keyStr);

		Cipher cipher;
		try {
			cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			p1 = byteToHexString(cipher.doFinal(val.getBytes()));
			return p1;
		} catch (Exception e23) {
		}
		return p1;
	}

	/**
	 * 解密
	 *
	 * @param keyStr
	 * @param sno
	 * @return
	 */
	public static String decrypt(String keyStr, String sno) {
		String p1 = "";
		Key key = getKey(keyStr);

		Cipher cipher;
		try {
			cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			p1 = new String(cipher.doFinal(hexStringToByte(sno)));
			return p1;

		} catch (Exception e23) {
		}
		return p1;
	}

	/**
	 * 把加密用key进行MD5加密
	 *
	 * @param keyStr
	 * @return
	 */
	private static SecretKeySpec getKey(String keyStr) {
		SecretKeySpec key = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(keyStr.getBytes());
			byte[] sha256Key = md.digest();

			MessageDigest md2 = MessageDigest.getInstance("MD5");
			md2.update(sha256Key);
			byte[] md5Key = md2.digest();

			key = new SecretKeySpec(md5Key, "Blowfish");

		} catch (Exception e) {
		}
		return key;
	}

	/**
	 * byte[] 转换成 16进制
	 *
	 * @param bts
	 * @return
	 */
	private static String byteToHexString(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}

	/**
	 * 16进制转换成byte[]
	 *
	 * @return byte[]
	 */
	private static byte[] hexStringToByte(String src) {
		src = src.trim().replace(" ", "").toUpperCase(Locale.US);
		int m = 0, n = 0;
		int iLen = src.length() / 2;
		byte[] ret = new byte[iLen];

		for (int i = 0; i < iLen; i++) {
			m = i * 2 + 1;
			n = m + 1;
			ret[i] = (byte) (Integer.decode("0x" + src.substring(i * 2, m) + src.substring(m, n)) & 0xFF);
		}
		return ret;
	}
}
