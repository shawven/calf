package com.test.support.log;

import com.test.support.util.IpUtils;

import javax.servlet.http.HttpServletRequest;
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

    public String ipAddress;

    public String path;

    public Map<String, String[]> parameters;

    public Map<String, String> headers;

    public RequestInfo(HttpServletRequest request) {
        ip = IpUtils.getIp(request);
        ipAddress = IpUtils.getIpAddress(ip);
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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
}
