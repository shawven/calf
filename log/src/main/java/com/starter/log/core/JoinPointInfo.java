package com.starter.log.core;

import com.starter.log.annotation.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * @author Shoven
 * @date 2019-07-26 9:04
 */

public class JoinPointInfo {

    private JoinPoint joinPoint;

    public JoinPointInfo(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public Log getLog() {
        return LogAnnotationUtils.getMethodLogAnnotation(getMethod(), Log.class);
    }

    public Class<?> getTypeClass() {
        return joinPoint.getTarget().getClass();
    }

    public Method getMethod() {
        return ((MethodSignature) joinPoint.getSignature()).getMethod();
    }
}
