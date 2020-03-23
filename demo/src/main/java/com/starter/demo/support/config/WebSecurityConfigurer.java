package com.starter.demo.support.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author Shoven
 * @date 2019-05-09 15:40
 */
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/statics/**",
                "/favicon.ico");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().disable()
                .and().csrf().disable()
                .cors()
                .and().authorizeRequests().requestMatchers(CorsUtils::isPreFlightRequest).permitAll();

    }

    public CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }
}
