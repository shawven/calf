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

    public static String getIpAddress(String ip) {
        if ("127.0.0.1".equals(ip)) {
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
        return new HttpUtil().request().get(urlStr).withBrowserHeaders().execute();
    }


    public static String getIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR",
                "X-Real-IP"};
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
}
