package com.github.shawven.calf.payment.support;

import com.github.shawven.calf.payment.client.PaymentClientTypeEnum;
import com.github.shawven.calf.payment.domain.PaymentRequest;
import com.github.shawven.calf.payment.provider.PaymentProviderEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shoven
 * @date 2019-09-03
 */
public class PaymentLogger {

    private Logger logger;

    private PaymentLogger(Logger logger) {
        this.logger = logger;
    }

    public static PaymentLogger getLogger(Class<?> clazz) {
        return new PaymentLogger(LoggerFactory.getLogger(clazz));
    }

    public void info(PaymentRequest request, String s, Object var) {
        logger.info(addContextInfo(request, s), var);
    }

    public void info(PaymentRequest request, String s, Object... var) {
        logger.info(addContextInfo(request, s), var);
    }

    public void rawInfo(String s, Object var) {
        logger.info(s, var);
    }

    public void rawInfo(String s, Object... var) {
        logger.info(s, var);
    }

    public void warn(PaymentRequest request, String s, Object var) {
        logger.warn(addContextInfo(request, s), var);
    }

    public void warn(PaymentRequest request, String s, Object... var) {
        logger.warn(addContextInfo(request, s), var);
    }

    public void rawWarn(String s, Object var) {
        logger.warn(s, var);
    }

    public void rawWarn(String s, Object... var) {
        logger.warn(s, var);
    }

    public void error(PaymentRequest request, String s, Object var) {
        logger.error(addContextInfo(request, s), var);
    }

    public void error(PaymentRequest request, String s, Object... var) {
        logger.error(addContextInfo(request, s), var);
    }

    public void rawError(String s, Object var) {
        logger.error(s, var);
    }

    public void rawError(String s, Object... var) {
        logger.error(s, var);
    }

    public void debug(PaymentRequest request, String s, Object var) {
        logger.debug(addContextInfo(request, s), var);
    }

    public void debug(PaymentRequest request, String s, Object... var) {
        logger.debug(addContextInfo(request, s), var);
    }

    public void rawDebug(String s, Object var) {
        logger.debug(s, var);
    }

    public void rawDebug(String s, Object... var) {
        logger.debug(s, var);
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    private String addContextInfo(PaymentRequest request, String s) {
        if (request == null) {
            return s;
        }
        StringBuilder builder = new StringBuilder();
        String principal = request.getPrincipal();
        if (principal != null) {
            builder.append("[").append(principal).append("]");
        }
        PaymentProviderEnum paymentProvider = request.getPaymentProvider();
        if (paymentProvider != null) {
            builder.append(paymentProvider.getName());
        }
        PaymentClientTypeEnum paymentClientType = request.getPaymentClientType();
        if (paymentClientType != null) {
            builder.append(paymentClientType.getName());
        }
        return builder.append(s).toString();
    }
}
