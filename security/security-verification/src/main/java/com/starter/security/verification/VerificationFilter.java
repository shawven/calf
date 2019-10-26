
package com.starter.security.verification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starter.security.base.ResponseData;
import com.starter.security.verification.properties.VerificationProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 校验验证码的过滤器
 */

public class VerificationFilter extends OncePerRequestFilter implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(VerificationFilter.class);

    /**
     * 系统中的校验码处理器
     */
    private VerificationProcessorHolder verificationProcessorHolder;

    private VerificationProperties securityProperties;

    /**
     * 存放所有需要校验验证码的url
     */
    private Map<String, VerificationType> urlMap;
    /**
     * 验证请求url与配置的url是否匹配的工具类
     */
    private AntPathMatcher pathMatcher;

    private ObjectMapper objectMapper;

    public VerificationFilter(VerificationProcessorHolder verificationProcessorHolder,
                              VerificationProperties verificationProperties) {
        urlMap = new HashMap<>();
        pathMatcher = new AntPathMatcher();
        objectMapper = new ObjectMapper();
        this.verificationProcessorHolder = verificationProcessorHolder;
        this.securityProperties = verificationProperties;
    }

    /**
     * 初始化要拦截的url配置信息
     */
    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
        addUrlToMap(securityProperties.getCaptcha().getUrl(), VerificationType.IMAGE);
        addUrlToMap(securityProperties.getSms().getUrl(), VerificationType.SMS);
    }

    /**
     * 讲系统中配置的需要校验验证码的URL根据校验的类型放入map
     *
     * @param urlString
     * @param type
     */
    protected void addUrlToMap(String urlString, VerificationType type) {
        if (StringUtils.isNotBlank(urlString)) {
            String[] urls = StringUtils.splitByWholeSeparatorPreserveAllTokens(urlString, ",");
            for (String url : urls) {
                urlMap.put(url, type);
            }
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see
     * org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        VerificationType type = getVerificationType(request);
        if (type != null) {
            String name = type.getName();
            logger.info("正在校验" + name + "验证码");
            try {
                verificationProcessorHolder.get(type)
                        .verification(new ServletWebRequest(request, response));
                logger.info(name + "验证码校验通过");
            } catch (VerificationException e) {
                String message = e.getMessage();
                logger.info(name + "校验失败：{}", message);
                responseErrorMessage(response, message);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void responseErrorMessage(HttpServletResponse response, String message) {
        try {
            ResponseData result = new ResponseData(400, message);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(400);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * 获取校验码的类型，如果当前请求不需要校验，则返回null
     *
     * @param request
     * @return
     */
    private VerificationType getVerificationType(HttpServletRequest request) {
        VerificationType result = null;
        Set<String> urls = urlMap.keySet();
        for (String url : urls) {
            if (pathMatcher.match(url, request.getRequestURI())) {
                result = urlMap.get(url);
            }
        }
        return result;
    }

}
