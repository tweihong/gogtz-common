package com.gogtz.common.http;

import com.gogtz.common.string.StringPool;
import com.gogtz.common.validator.Validator;

public class HttpUtil {
    public static String encodeURL(String url) {
        return encodeURL(url, false);
    }

    public static String encodeURL(String url, boolean escapeSpaces) {
        if (Validator.isNull(url)) {
            return url;
        }

        return URLCodec.encodeURL(url, StringPool.UTF8, escapeSpaces);
    }
}
