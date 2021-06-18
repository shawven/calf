package com.starter.demo.support.lock;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Shoven
 * @date 2020-10-29
 */
public class SpELUtils {

    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public static String parse(JoinPoint joinPoint, String[] expressions) {
        Method method = resolveMethod(joinPoint);
        List<String> names = resolveNames(expressions, method, joinPoint.getArgs());
        return String.join("_", names);
    }

    private static List<String> resolveNames(String[] expressions, Method method, Object[] parameterValues) {
        List<String> names = new ArrayList<>();
        EvaluationContext context = new MethodBasedEvaluationContext(null, method, parameterValues, NAME_DISCOVERER);
        for (String expression : expressions) {
            if (StringUtils.isNotBlank(expression)) {
                Object objKey = PARSER.parseExpression(expression).getValue(context);
                names.add(ObjectUtils.nullSafeToString(objKey));
            }
        }
        return names;
    }

    private static Method resolveMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        Method method = getDeclaredMethodFor(targetClass, signature.getName(),
                signature.getMethod().getParameterTypes());
        if (method == null) {
            throw new IllegalStateException("Cannot resolve target method: " + signature.getMethod().getName());
        }
        return method;
    }

    private static Method getDeclaredMethodFor(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getDeclaredMethodFor(superClass, name, parameterTypes);
            }
        }
        return null;
    }
}
