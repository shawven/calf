package com.starter.security.browser;

import com.starter.security.browser.config.BrowserConfiguration;
import com.starter.security.browser.properties.BrowserProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {BrowserConfiguration.class, BrowserProperties.class})
public @interface EnableBrowserSecuritySupport {
}
