
package com.starter.security.verification;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * 校验码处理器管理器
 */

public class VerificationProcessorHolder {

    private Map<VerificationType, VerificationProcessor> verificationProcessors;

    public VerificationProcessorHolder(List<VerificationProcessor> verificationProcessors) {
        this.verificationProcessors = verificationProcessors.stream()
                .collect(toMap(
                        processor -> {
                            String className = processor.getClass().getSimpleName();
                            String typeName = StringUtils.substringBefore(className, "Processor").toUpperCase();
                            return VerificationType.valueOf(typeName);
                        },
                        identity())
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
