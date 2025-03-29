package com.ajaxjs.util;

import lombok.extern.slf4j.Slf4j;

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
}
