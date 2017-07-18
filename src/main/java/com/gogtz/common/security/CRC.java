package com.gogtz.common.security;

/**
 * Created by hanQ on 2017/4/14.
 */

public class CRC {

    /*根据字符串生成CRC*/
    public static final String generate(String signData) {
		String md5Data = MD5Util.string2MD5(signData);

        long md5Sign = crc32(md5Data);
        long dataSign = crc32(signData);

        String value = dataSign + "" + md5Sign + "" + signData.length();
        return value;
    }

    /*计算CRC32*/
    public static long crc32(String data) {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(data.getBytes());
        return crc.getValue();
    }

}
