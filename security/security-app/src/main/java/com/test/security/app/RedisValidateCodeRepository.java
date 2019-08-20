
package com.test.security.app;

import com.test.security.validate.ValidateCode;
import com.test.security.validate.ValidateCodeException;
import com.test.security.validate.ValidateCodeRepository;
import com.test.security.validate.ValidateCodeType;
import com.test.security.validate.property.ValidationConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * 基于redis的验证码存取器，避免由于没有session导致无法存取验证码的问题
 *
 * @author Shoven
 * @since 2019-05-08 21:51
 */
public class RedisValidateCodeRepository implements ValidateCodeRepository {

	private RedisTemplate<Object, Object> redisTemplate;

    private final static String PARAM_NAME = ValidationConstants.DEFAULT_PARAMETER_NAME_MOBILE;

    public RedisValidateCodeRepository(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
	public void save(ServletWebRequest request, ValidateCode code, ValidateCodeType type) {
        redisTemplate.opsForValue().set(getKey(request, type), code, code.getExpireIn(), TimeUnit.SECONDS);
	}

	@Override
	public ValidateCode get(ServletWebRequest request, ValidateCodeType type) {
		Object value = redisTemplate.opsForValue().get(getKey(request, type));
		if (value == null) {
			return null;
		}
		return (ValidateCode) value;
	}

	@Override
	public void remove(ServletWebRequest request, ValidateCodeType type) {
		redisTemplate.delete(getKey(request, type));
	}

	/**
	 * @param request
	 * @param type
	 * @return
	 */
	@Override
    public String getKey(ServletWebRequest request, ValidateCodeType type) {
        String uniqueId = getUniqueId(request.getRequest());
        return "code:" + type.getLabel() + ":" + uniqueId;
	}

    private String getUniqueId(HttpServletRequest request) {
	    // 先尝试获取手机号
        String mobile = request.getParameter(PARAM_NAME);
        String uniqueId = Objects.isNull(mobile) ? String.valueOf(request.getAttribute(PARAM_NAME)) : null;
        if (uniqueId != null) {
            return uniqueId;
        }

        // 再尝试获取设备id
        String deviceId = request.getHeader("deviceId");
        if (StringUtils.isBlank(deviceId)) {
            throw new ValidateCodeException("请在请求头中携带deviceId参数");
        }
        return deviceId;
    }
}
