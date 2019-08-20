
package com.test.security.validate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.security.core.ResponseData;
import com.test.security.validate.property.ValidationProperties;
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

public class ValidateCodeFilter extends OncePerRequestFilter implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ValidateCodeFilter.class);

    /**
     * 系统中的校验码处理器
     */
    private ValidateCodeProcessorHolder validateCodeProcessorHolder;

    private ValidationProperties securityProperties;

    /**
     * 存放所有需要校验验证码的url
     */
    private Map<String, ValidateCodeType> urlMap;
    /**
     * 验证请求url与配置的url是否匹配的工具类
     */
    private AntPathMatcher pathMatcher;

    private ObjectMapper objectMapper;

    public ValidateCodeFilter(ValidateCodeProcessorHolder validateCodeProcessorHolder,
                              ValidationProperties validationProperties) {
        urlMap = new HashMap<>();
        pathMatcher = new AntPathMatcher();
        objectMapper = new ObjectMapper();
        this.validateCodeProcessorHolder = validateCodeProcessorHolder;
        this.securityProperties = validationProperties;
    }

    /**
     * 初始化要拦截的url配置信息
     */
    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
        addUrlToMap(securityProperties.getImage().getUrl(), ValidateCodeType.IMAGE);
        addUrlToMap(securityProperties.getSms().getUrl(), ValidateCodeType.SMS);
    }

    /**
     * 讲系统中配置的需要校验验证码的URL根据校验的类型放入map
     *
     * @param urlString
     * @param type
     */
    protected void addUrlToMap(String urlString, ValidateCodeType type) {
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
        ValidateCodeType type = getValidateCodeType(request);
        if (type != null) {
            String name = type.getName();
            logger.info("正在校验" + name + "验证码");
            try {
                validateCodeProcessorHolder.findValidateCodeProcessor(type)
                        .validate(new ServletWebRequest(request, response));
                logger.info(name + "验证码校验通过");
            } catch (ValidateCodeException e) {
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
    private ValidateCodeType getValidateCodeType(HttpServletRequest request) {
        ValidateCodeType result = null;
        Set<String> urls = urlMap.keySet();
        for (String url : urls) {
            if (pathMatcher.match(url, request.getRequestURI())) {
                result = urlMap.get(url);
            }
        }
        return result;
    }

}
