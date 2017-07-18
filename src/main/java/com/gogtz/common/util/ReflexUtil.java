package com.gogtz.common.util;

import java.lang.reflect.Method;

public class ReflexUtil {
	// getMethod
	public static Object invokeMethod(String propertiesName, Object object) {
		try {
			if (object == null)
				return null;
			if (!propertiesName.contains(".")) {
				String methodName = "get" + getMethodName(propertiesName);
				Method method = object.getClass().getMethod(methodName);
				return method.invoke(object);
			}
			String methodName = "get" + getMethodName(propertiesName.substring(0, propertiesName.indexOf(".")));
			Method method = object.getClass().getMethod(methodName);
			return invokeMethod(propertiesName.substring(propertiesName.indexOf(".") + 1), method.invoke(object));

		} catch (Exception e) {
			return null;
		}
	}

	private static String getMethodName(String fildeName) {
		byte[] items = fildeName.getBytes();
		items[0] = (byte) ((char) items[0] - 'a' + 'A');
		return new String(items);
	}
}
