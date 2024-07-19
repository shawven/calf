package com.github.shawven.calf.practices.demo.support.validator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

public class ValidationUtils {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 验证返回结果
     *
     * @param obj
     * @return
     */
    public static Map<String, String> validate(Object obj) {
        Map<String, StringBuilder> errorMap = new LinkedHashMap<>();

        Set<ConstraintViolation<Object>> result = validator.validate(obj, Default.class);
        if (!result.isEmpty()) {
            result.stream()
                    .sorted(Comparator.comparing(c -> c.getPropertyPath().toString()))
                    .forEach(cv -> {
                        //这里循环获取错误信息，可以自定义格式
                        String property = cv.getPropertyPath().toString();
                        StringBuilder sb = errorMap.computeIfAbsent(property, s -> new StringBuilder());
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(cv.getMessage());
                    });
        }
        return errorMap.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().toString()));
    }


    /**
     * 验证抛出异常
     *
     * @param obj
     * @throws ConstraintViolationException
     */
    public static void validateThrowException(Object obj) throws ConstraintViolationException {
        Set<ConstraintViolation<Object>> result = validator.validate(obj, Default.class);
        if (!result.isEmpty()) {
            throw new ConstraintViolationException(result);
        }
    }
}

