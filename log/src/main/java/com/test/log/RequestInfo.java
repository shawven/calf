package com.test.log;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-07-25 16:54
 */

public class RequestInfo {

    public String method;

    public String ip;

    public String path;

    public Map<String, String[]> parameters;

    public Map<String, String> headers;

    public RequestInfo(HttpServletRequest request) {
        ip = getIp(request);
        method = request.getMethod();
        path = request.getRequestURL().toString();
        parameters = request.getParameterMap();

        headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public static String getIp(HttpServletRequest request) {
        String[] headers = {"x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        String ip = null;
        boolean found = false;
        for (String header : headers) {
            ip = request.getHeader(header);
            if (ip != null && !"unknown".equalsIgnoreCase(ip)) {
                found = true;
                break;
            }
        }
        if (!found) {
            ip = request.getRemoteAddr();
        }
        String localhost = "127.0.0.1";
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
}
