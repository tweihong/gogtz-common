package com.gogtz.common.string;

import java.math.BigDecimal;

public class GetterUtil {

    /**
     * 取得String
     *
     * @param obj
     * @return
     */
    public static String getString(Object obj) {
        if (obj == null) {
            return "";
        }
        return String.valueOf(obj);
    }

    /**
     * 取得String
     *
     * @param obj
     * @param val 默认值
     * @return
     */
    public static String getString(Object obj, String val) {
        if (obj == null) {
            return val;
        }
        return String.valueOf(obj);
    }

    /**
     * 取得double
     *
     * @param obj
     * @return
     */
    public static double getDouble(Object obj) {
        if (obj == null) {
            return 0.0;
        }
        try {
            return Double.valueOf(String.valueOf(obj));
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 取得long
     *
     * @param obj
     * @return
     */
    public static long getLong(Object obj) {
        if (obj == null) {
            return 0;
        }

        try {
            return Long.valueOf(String.valueOf(obj));
        } catch (Exception e) {
            return 0;
        }

    }

    /**
     * 取得int
     *
     * @param obj
     * @return
     */
    public static int getInteger(Object obj) {
        if (obj == null) {
            return 0;
        }

        try {
            return Integer.valueOf(String.valueOf(obj));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 取得int
     *
     * @param obj
     * @return
     */
    public static int getInteger(Object obj, int defaultValue) {
        if (obj == null) {
            return defaultValue;
        }

        try {
            return Integer.valueOf(String.valueOf(obj));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 取得BigDecimal
     *
     * @param obj
     * @return
     */
    public static BigDecimal getBigDecimal(Object obj, int scale) {
        if (obj == null) {
            return new BigDecimal(0);
        }

        try {
            return new BigDecimal(obj.toString()).setScale(scale, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            return new BigDecimal(0);
        }
    }
}
