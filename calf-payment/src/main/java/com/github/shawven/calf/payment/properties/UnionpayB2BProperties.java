package com.github.shawven.calf.payment.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shoven
 * @date 2019-09-17
 */
@Configuration
@ConfigurationProperties(prefix = "unionpay.b2b")
public class UnionpayB2BProperties extends UnionpayProperties {

}
