package com.test.app.support.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ip工具
 *
 * @author Shoven
 * @date 2016年8月12日 下午2:43:34
 */

public class IpUtils {

    private static String localhost = "127.0.0.1";

    public static String getIpAddress(String ip) {
        if (localhost.equals(ip)) {
            return "内网IP";
        }

        Map result;
        try {
            String responseStr = httpGet("http://ip.taobao.com/service/getIpInfo.php?ip=" + ip);
            result = new ObjectMapper().readValue(responseStr, Map.class);
        } catch (IOException e) {
            return null;
        }
        if (!"0".equals(result.get("code").toString())) {
            return null;
        }

        Map addressInfo = (Map) result.get("data");
        Map<String, Object> data = new LinkedHashMap<>();
        // 国家
        data.put("country", addressInfo.get("country"));
        // 地区
        data.put("area", addressInfo.get("area"));
        // 省份
        data.put("region", addressInfo.get("region"));
        // 市区
        data.put("city", addressInfo.get("city"));
        // 地区
        data.put("county", addressInfo.get("county"));
        // ISP公司
        data.put("isp", addressInfo.get("isp"));

        StringBuilder address = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                String str = entry.getValue().toString();
                if (StringUtils.isNotBlank(str) &&!"XX".equals(str.toUpperCase())) {
                    address.append(str).append("-");
                }
            }
        }

        String s = address.toString();
        return s.length() > 0 ? s.substring(0,  s.length() - 1) : null;
    }


    /**
     * @param urlStr   请求的地址
     * @return
     */
    private static String httpGet(String urlStr) throws IOException {
        return HttpClientUtils.executor()
                .get(urlStr)
                .withBrowserHeaders()
                .execute();
    }


    public static String getIp(HttpServletRequest request) {
        String[] headers = {"x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        String ip = null;
        boolean found = false;
        for (String header : headers) {
            ip = request.getHeader(header);
            if (!strIsBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
                found = true;
                break;
            }
        }
        if (!found) {
            ip = request.getRemoteAddr();
        }
        if (localhost.equals(ip)) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ignored) {}
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15 && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? localhost : ip;
    }

    private static boolean strIsBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }
}
