package com.ajaxjs.util;


import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Web 工具类
 */
@Slf4j
public class WebUtils {
    /**
     * 获取 IP
     *
     * @return ip
     */
    public static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Get IP failed.", e);
            return null;
        }
    }

    final static String UNKNOWN = "unknown";

    /**
     * The ip from browser.
     * 要外网访问才能获取到外网地址，如果你在局域网甚至本机上访问，获得的是内网或者本机的ip
     *
     * @param request The request object
     * @return The ip
     */
    public static String getClientIp(HttpServletRequest request) {
        String ipAddress = null;

        String ipAddresses = request.getHeader("X-Forwarded-For"); //X-Forwarded-For：Squid 服务代理

        if (ObjectHelper.isEmptyText(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses))
            ipAddresses = request.getHeader("X-Real-IP");   // X-Real-IP：nginx服务代理

        if (ObjectHelper.isEmptyText(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses))
            ipAddresses = request.getHeader("Proxy-Client-IP"); // Proxy-Client-IP：apache 服务代理

        if (ObjectHelper.isEmptyText(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses))
            ipAddresses = request.getHeader("HTTP_CLIENT_IP"); // HTTP_CLIENT_IP：有些代理服务器

        // 有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ObjectHelper.isEmptyText(ipAddresses))
            ipAddress = ipAddresses.split(",")[0];

        if (ObjectHelper.isEmptyText(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses))
            ipAddress = request.getRemoteAddr();

        return ipAddress;
    }

    /**
     * 尝试从 Cookie 中提取指定名称的 value
     *
     * @param request    The request object
     * @param cookieName The name of cookie
     * @return The value of the cookie
     */
    public static String getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (!ObjectHelper.isEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName()))
                    return cookie.getValue();
            }
        }

        return null;
    }
}
