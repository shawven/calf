
package com.test.security.validate;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * 校验码处理器管理器
 */

public class ValidateCodeProcessorHolder {

    private Map<ValidateCodeType, ValidateCodeProcessor> validateCodeProcessors;

    public ValidateCodeProcessorHolder(Map<String, ValidateCodeProcessor> validateCodeProcessors) {
        this.validateCodeProcessors = validateCodeProcessors.entrySet().stream()
                .collect(toMap(
                        entry -> ValidateCodeType.valueOf(
                                StringUtils.substringBefore(entry.getKey(), "CodeProcessor").toUpperCase()),
                        Map.Entry::getValue)
                );
    }

    /**
     * @param type
     * @return
     */
    public ValidateCodeProcessor findValidateCodeProcessor(ValidateCodeType type) {
        ValidateCodeProcessor processor = validateCodeProcessors.get(type);
        if (processor == null) {
            throw new ValidateCodeException(type.getName() + "验证码处理器不存在");
        }
        return processor;
    }
}
