/**
 * Description: Session工具类
 * Copyright: Copyright (Gogtz Corporation)2017
 * Company: Gogtz Corporation
 *
 * @author: t
 * @version: 1.0
 * Created at: 2017年7月17日15:26:21
 * Modification History:
 * Modified by :
 */
package com.gogtz.common.session;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import javax.servlet.http.HttpServletRequest;

public class SessionUtils {

    /**
     * 设置SESSION
     *
     * @return
     */
    public static void setSession(String key, String value) {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession(true);
        session.setAttribute(key, value);
    }

    /**
     * 设置SESSION
     *
     * @return
     */
    public static void setSession(String key, Object value) {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession(true);
        session.setAttribute(key, value);
    }

    /**
     * 获取SESSION
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSession(String key) {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession(true);
        return (T) session.getAttribute(key);
    }

    /**
     * 此处为方法说明
     *
     * @return
     * @author renxingchen
     */
    public static Session getSession() {
        Subject subject = SecurityUtils.getSubject();
        return subject.getSession(true);
    }

    /**
     * 清除SESSION
     *
     * @return
     */
    public static void removeSession(String key) {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession(true);
        session.removeAttribute(key);
    }

    /**
     * @param request
     * @return
     */
    public static String getWebRootPath(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serviceName = request.getServerName();
        String port = String.valueOf(request.getServerPort());
        String webPath = request.getContextPath();
        String basePath = scheme + "://" + serviceName + ":" + port + webPath;
        return basePath;
    }
}
