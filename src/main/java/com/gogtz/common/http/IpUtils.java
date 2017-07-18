package com.gogtz.common.http;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP地址工具类,
 */
public class IpUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(IpUtils.class);
    /**
     * 未知
     */
    private static final String UNKNOWN = "unknown";

    /**
     * 隐藏构造器
     */
    protected IpUtils() {
    }

    /**
     * 获取当前网络IP
     *
     * @param request HttpServletRequest
     * @return IP地址
     */
    public static String getIpAddr(final HttpServletRequest request) {
        if (null == request) {
            return UNKNOWN;
        }
        String ipAddress = request.getHeader("x-forwarded-for");
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip) || StringUtils.equalsIgnoreCase(UNKNOWN, ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || StringUtils.equalsIgnoreCase(UNKNOWN, ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (StringUtils.isBlank(ip) || StringUtils.equalsIgnoreCase(UNKNOWN, ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || StringUtils.equalsIgnoreCase(UNKNOWN, ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || StringUtils.equals("unknown", ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (StringUtils.equalsIgnoreCase("127.0.0.1", ipAddress)
                    || StringUtils.equalsIgnoreCase("0:0:0:0:0:0:0:1", ipAddress)) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    LOGGER.error("[StringUtil.getIpAddr]", e);
                }
            }
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        // "***.***.***.***".length() = 15
        if (StringUtils.isNotBlank(ipAddress) && 15 < ipAddress.length()) {
            int idx = ipAddress.indexOf(",");
            if (-1 < idx) {
                ipAddress = ipAddress.substring(0, idx);
            }
        }
        return ipAddress;
    }
}
