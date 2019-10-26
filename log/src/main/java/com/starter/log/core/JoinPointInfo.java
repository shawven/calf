package com.starter.log.core;

import com.starter.log.annotation.Log;
import com.starter.log.emun.LogType;

import java.lang.reflect.Method;

/**
 * @author Shoven
 * @date 2019-07-26 9:04
 */

public class JoinPointInfo {

    private LogType logType;

    private Log logAnnotation;

    private Class typeClass;

    private Method method;

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public Log getLogAnnotation() {
        return logAnnotation;
    }

    public void setLogAnnotation(Log logAnnotation) {
        this.logAnnotation = logAnnotation;
    }

    public Class getTypeClass() {
        return typeClass;
    }

    public void setTypeClass(Class typeClass) {
        this.typeClass = typeClass;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
