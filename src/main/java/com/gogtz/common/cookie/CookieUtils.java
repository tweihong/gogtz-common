/**
 * Description: Cookie工具类
 * Copyright: Copyright (Gogtz Corporation)2017
 * Company: Gogtz Corporation
 *
 * @author: t
 * @version: 1.0
 * Created at: 2017年7月17日15:26:21
 * Modification History:
 * Modified by :
 */
package com.gogtz.common.cookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class CookieUtils {
    /**
     * 保存cookie
     *
     * @param name
     * @param value
     * @param response
     */
    public static void setCookie(String name, String value, HttpServletResponse response) {
        setCookie(name, value, -1, response);
    }

    /**
     * 保存cookie
     *
     * @param name
     * @param value
     * @param time
     * @param response
     */
    public static void setCookie(String name, String value, int time, HttpServletResponse response) {
        Cookie c = new Cookie(name, value);
        c.setMaxAge(time);
        response.addCookie(c);
    }

    /**
     * 判断cookie是否存在
     *
     * @param request
     * @param name    cookie名字
     * @return
     */
    public static boolean hasCookie(HttpServletRequest request, String name) {
        Map<String, Cookie> cookieMap = ReadCookieMap(request);
        if (cookieMap.containsKey(name)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据名字获取cookie
     *
     * @param request
     * @param name    cookie名字
     * @return
     */
    public static Cookie getCookieByName(HttpServletRequest request, String name) {
        Map<String, Cookie> cookieMap = ReadCookieMap(request);
        if (cookieMap.containsKey(name)) {
            Cookie cookie = (Cookie) cookieMap.get(name);
            return cookie;
        } else {
            return null;
        }
    }

    /**
     * 将cookie封装到Map里面
     *
     * @param request
     * @return
     */
    private static Map<String, Cookie> ReadCookieMap(HttpServletRequest request) {
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        Cookie[] cookies = request.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie);
            }
        }
        return cookieMap;
    }

}
