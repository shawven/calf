
package com.test.security.social.config;

import com.test.security.social.MyJdbcUsersConnectionRepository;
import com.test.security.social.properties.QQProperties;
import com.test.security.social.properties.SocialProperties;
import com.test.security.social.properties.WeixinProperties;
import com.test.security.social.qq.connet.QQConnectionFactory;
import com.test.security.social.support.SocialAuthenticationFilterPostProcessor;
import com.test.security.social.support.SocialConfigurer;
import com.test.security.social.support.SocialConfigurerProcessor;
import com.test.security.social.weixin.connect.WeixinConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.*;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.social.security.AuthenticationNameUserIdSource;

import javax.sql.DataSource;
import java.util.List;

/**
 * 社交登录配置主类
 */
@Configuration
@EnableSocial
public class SocialConfiguration extends SocialConfigurerAdapter {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SocialProperties socialProperties;

	@Autowired(required = false)
    private List<ConnectionFactory<?>> connectionFactories;

    @Autowired(required = false)
	private ConnectionSignUp connectionSignUp;

	@Autowired(required = false)
	private SocialAuthenticationFilterPostProcessor socialAuthenticationFilterPostProcessor;

    @Autowired(required = false)
    private SocialConfigurerProcessor socialConfigurerProcessor;

    @Override
    public UserIdSource getUserIdSource() {
        return new AuthenticationNameUserIdSource();
    }

	/** (non-Javadoc)
	 * @see org.springframework.social.config.annotation.SocialConfigurerAdapter#getUsersConnectionRepository(org.springframework.social.connect.ConnectionFactoryLocator)
	 */
	@Override
	public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        MyJdbcUsersConnectionRepository repository = new MyJdbcUsersConnectionRepository(dataSource,
				connectionFactoryLocator, Encryptors.noOpText());

		// 配置无感知注册处理程序，用户第一次登陆时需要注册的，调用该程序处理
        // org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository.findUserIdsWithConnection
        // 上面那个类里面判断了如果当前社交用户还不存在当前系统且配置了 connectionSignUp
        // 那么就可以调用去注册，并且返回系统的userId
        // 如果没有配置就会在org.springframework.social.security.SocialAuthenticationFilter.doAuthentication
        // 跳转到SocialConfigurer.signupUrl()配置的注册页面
        if(connectionSignUp != null) {
			repository.setConnectionSignUp(connectionSignUp);
		}
		return repository;
	}

    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer connectionFactoryConfigurer, Environment environment) {
        if (connectionFactories != null) {
            connectionFactories.forEach(connectionFactoryConfigurer::addConnectionFactory);
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.security.social.weixin", name = "app-id")
    public ConnectionFactory<?> createWeixinConnectionFactory() {
        WeixinProperties weixinConfig = socialProperties.getWeixin();
        return new WeixinConnectionFactory(weixinConfig.getProviderId(), weixinConfig.getAppId(),
                weixinConfig.getAppSecret());
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.security.social.qq", name = "app-id")
    public ConnectionFactory<?> createQqConnectionFactory() {
        QQProperties qqConfig = socialProperties.getQq();
        return new QQConnectionFactory(qqConfig.getProviderId(), qqConfig.getAppId(),
                qqConfig.getAppSecret());
    }

    /**
	 * 社交登录配置类，供浏览器或app模块引入设计登录配置用。
	 * @return
	 */
	@Bean
	public SocialConfigurer socialSecurityConfig() {
        // 设置过滤器拦截社交登录的url
		String filterProcessesUrl = socialProperties.getFilterProcessesUrl();
		SocialConfigurer configurer = new SocialConfigurer(filterProcessesUrl);

		// 设置社交登录判断是第一次登录时需要跳转的页面，需要引导用户进行注册或绑定
		// 如果没有配置 connectionSignUp 那么 org.springframework.social.security.SocialAuthenticationFilter.doAuthentication
        // 的方法会跳转到这里配置的注册页面
		configurer.signupUrl(socialProperties.getSignUpUrl());

		// 设置过滤器链的后处理器，例如app环境下的成功处理器与浏览器环境会不同
		configurer.setSocialAuthenticationFilterPostProcessor(socialAuthenticationFilterPostProcessor);

		// 不同环境下的社交配置不一样
		if (socialConfigurerProcessor != null) {
            socialConfigurerProcessor.process(configurer);
        }
		return configurer;
	}

	/**
	 * 用来处理注册流程的工具类
	 *
	 * @param connectionFactoryLocator
	 * @return
	 */
	@Bean
	public ProviderSignInUtils providerSignInUtils(ConnectionFactoryLocator connectionFactoryLocator) {
		return new ProviderSignInUtils(connectionFactoryLocator,
                getUsersConnectionRepository(connectionFactoryLocator));
	}

    @Bean
    public ConnectController connectController(ConnectionFactoryLocator connectionFactoryLocator,
                                               ConnectionRepository connectionRepository) {
        return new ConnectController(connectionFactoryLocator, connectionRepository);
    }
}
