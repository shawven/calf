package com.test.payment.properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Shoven
 * @date 2019-09-17
 */
@Configuration
@ConfigurationProperties(prefix = "unionpay")
public class UnionpayB2CProperties extends UnionpayProperties{

}
