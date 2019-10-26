
package com.starter.security.verification;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * 校验码处理器管理器
 */

public class VerificationProcessorHolder {

    private Map<VerificationType, VerificationProcessor> verificationProcessors;

    public VerificationProcessorHolder(Map<String, VerificationProcessor> verificationProcessors) {
        this.verificationProcessors = verificationProcessors.entrySet().stream()
                .collect(toMap(
                        entry -> VerificationType.valueOf(
                                StringUtils.substringBefore(entry.getKey(), "CodeProcessor").toUpperCase()),
                        Map.Entry::getValue)
                );
    }

    /**
     * @param type
     * @return
     */
    public VerificationProcessor get(VerificationType type) {
        VerificationProcessor processor = verificationProcessors.get(type);
        if (processor == null) {
            throw new VerificationException(type.getName() + "验证码处理器不存在");
        }
        return processor;
    }
}
