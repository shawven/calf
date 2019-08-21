package com.test.log.core;

import org.aspectj.lang.JoinPoint;

/**
 * @author Shoven
 * @date 2019-07-26 9:03
 */
public interface JoinPointExtractor {

    JoinPointInfo extract(JoinPoint joinPoint);
}
