
package com.starter.security.browser.session;

import com.starter.security.browser.properties.BrowserProperties;
import org.springframework.security.web.session.InvalidSessionStrategy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 默认的session失效处理策略
 *
 * @author Shoven
 * @since 2019-05-08 21:53
 */
public class BrowserInvalidSessionStrategy extends AbstractSessionStrategy implements InvalidSessionStrategy {

	public BrowserInvalidSessionStrategy(BrowserProperties browserProperties) {
		super(browserProperties);
	}

	@Override
	public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		onSessionInvalid(request, response);
	}

}
