/*
 * Copyright(c) 2012-2017 JD Pharma.Ltd. All Rights Reserved.
 * 
 */
package com.gogtz.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.codec.Base64;

import java.io.*;

/**
 * 此处为类说明
 * 
 * @author renxingchen
 * @version zxpt 1.0
 * @since zxpt 1.0 2017年5月4日
 */
public class SerializeUtil {

	public static Object deserialize(String str) {
		ByteArrayInputStream bis = null;
		ObjectInputStream ois = null;
		try {
			if (StringUtils.isNotBlank(str)) {
				bis = new ByteArrayInputStream(Base64.decode(str));
				ois = new ObjectInputStream(bis);
				return ois.readObject();
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException("deserialize session error", e);
		} finally {
			try {
				if (null != ois) {
					ois.close();
				}
				if (null != ois) {
					bis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static String serialize(Object obj) {
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			return Base64.encodeToString(bos.toByteArray());
		} catch (Exception e) {
			throw new RuntimeException("serialize session error", e);
		} finally {
			try {
				oos.close();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
