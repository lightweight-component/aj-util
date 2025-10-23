/*
 * Copyright (C) 2025 Frank Cheung<frank@ajaxjs.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ajaxjs.util;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * String URL/Base64 encoder
 */
@Slf4j
public class EncodeTools {
    /**
     * BASE64 编码
     *
     * @param bytes 待编码的字符串 bytes
     * @return 已编码的字符串。使用 ISO-8859-1 字符集，请注意区别
     */
    public static String base64EncodeToString(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
