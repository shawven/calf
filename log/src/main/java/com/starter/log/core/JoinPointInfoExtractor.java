package com.starter.log.core;

import com.starter.log.annotation.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * @author Shoven
 * @date 2019-07-26 16:03
 */
public class JoinPointInfoExtractor implements JoinPointExtractor {

    @Override
    public JoinPointInfo extract(JoinPoint jp) {
        JoinPointInfo joinPointInfo = new JoinPointInfo();
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        Log log = LogAnnotationUtils.getMethodLogAnnotation(method, Log.class);

        joinPointInfo.setLogAnnotation(log);
        joinPointInfo.setMethod(method);
        joinPointInfo.setLogType(log.type());
        joinPointInfo.setTypeClass(jp.getTarget().getClass());

        return joinPointInfo;
    }
}

