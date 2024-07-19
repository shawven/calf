package com.github.shawven.calf.practices.demo.support.validator;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * 校验枚举值有效性，例如待校验的String、Number是否符合枚举值
 * @author kingdee
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValue.Validator.class)
public @interface EnumValue {

    String message() default "枚举值无效";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> enumClass();

    String enumMethod() default "";

    class Validator implements ConstraintValidator<EnumValue, Object> {

        private Class<? extends Enum<?>> enumClass;
        private String enumMethod;

        @Override
        public void initialize(EnumValue enumValue) {
            enumMethod = enumValue.enumMethod();
            enumClass = enumValue.enumClass();
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
            if (value == null) {
                return true;
            }
            if (enumClass == null || enumMethod == null) {
                return true;
            }

            Class<?> valueClass = value.getClass();
            // 类型相等无需校验
            if (valueClass.equals(enumClass)) {
                return true;
            }

            try {
                Method method = getMethod(valueClass);
                for (Object obj : enumClass.getEnumConstants()) {
                    if(value.equals(method.invoke(obj))){
                        return true;
                    }
                }
                return false;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(String.format("This %s(%s) method does not exist in the %s", enumMethod, value, enumClass), e);
            }
        }

        private Method getMethod(Class<?> aClass) throws NoSuchMethodException {
            if (enumMethod.isEmpty()) {
                if (String.class.isAssignableFrom(aClass)) {
                    return enumClass.getMethod("name");
                }
                if (Number.class.isAssignableFrom(aClass)) {
                    return enumClass.getMethod("ordinal");
                }
            }
            return enumClass.getMethod(enumMethod);
        }

    }
}
