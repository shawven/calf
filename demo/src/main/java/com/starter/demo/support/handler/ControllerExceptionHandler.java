package com.starter.demo.support.handler;

import com.starter.demo.support.Response;
import com.starter.demo.support.exception.BizException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;


/**
 * 控制器层全局异常处理器，业务异常BizException属于业务逻辑反馈(DEBUG输出)
 *
 * @author Shoven
 * @date  2018-11-09
 */
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    private final static String DEFAULT_MESSAGE = "系统发生错误，请稍后再试！";

    @Value("${spring.profiles.active}")
    private String active;

    private String[] ignoredProfiles = {"local", "dev", "test"};

    @Override
    @SuppressWarnings({"unchecked", "NullableProblems", "rawtypes"})
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatus status,
                                                             WebRequest request) {
        ResponseEntity<Response> rsp = Response.build(status, status.value(), ex.getMessage());
        return (ResponseEntity<Object>)(ResponseEntity)rsp;
    }

    /**
     * 处理数据绑定校验异常
     *
     * @param e
     * @param headers
     * @param status
     * @param request
     * @return
     */
    @Override
    @SuppressWarnings({"unchecked", "NullableProblems", "rawtypes"})
    protected ResponseEntity<Object> handleBindException(BindException e,
                                                         HttpHeaders headers,
                                                         HttpStatus status,
                                                         WebRequest request) {
        List<FieldError> fieldErrors = e.getFieldErrors();
        StringBuilder sb = new StringBuilder();
        for (FieldError fielderror : fieldErrors) {
            sb.append("[").append(fielderror.getField()).append("]")
                    .append(fielderror.getDefaultMessage()).append(", ");
        }
        String msg = sb.toString();
        msg = fieldErrors.size() > 0
                ? msg.substring(0, msg.length() - 2)
                : "请求的参数有误！";
        return (ResponseEntity<Object>)(ResponseEntity)Response.badRequest(msg);
    }

    /**
     * 处理业务异常
     *
     * @param e BizException
     * @return
     */
    @ExceptionHandler(BizException.class)
    @ResponseBody
    public ResponseEntity handleBizException(BizException e) {
        logger.debug(e.getMessage(), e);
        BizException last = e;
        List<Throwable> throwableList = ExceptionUtils.getThrowableList(e);
        if (throwableList.size() > 1) {
            for (Throwable throwable :  ExceptionUtils.getThrowableList(e)) {
                if (throwable instanceof BizException) {
                    last = (BizException) throwable;
                }
            }
        }
        return Response.unprocesable(getErrorMessage(last));
    }

    /**
     * 处理系统级异常，不应该将信息展示给用户
     *
     * @param e Exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity handleException(HttpServletRequest request, Exception e) {
        int index;
        if ((index = ExceptionUtils.indexOfType(e, BizException.class)) != - 1) {
            Throwable throwable = ExceptionUtils.getThrowableList(e).get(index);
            return handleBizException((BizException)throwable);
        }
        String requestDesc;
        Map<String, String[]> parameterMap = new HashMap<>(request.getParameterMap());
        parameterMap.remove("t");
        if (!parameterMap.isEmpty()) {
            Map<String, String> params = parameterMap.entrySet().stream()
                    .collect(toMap(Map.Entry::getKey, entry -> Arrays.toString(entry.getValue())));
            requestDesc = String.format("URL[%s], Params[%s]", request.getRequestURL(), params);
        } else {
            requestDesc = String.format("URL[%s]", request.getRequestURL());
        }
        logger.error(requestDesc + ":" + e.getMessage(), e);
        if (withDetail()) {
            if (e instanceof NullPointerException) {
                StackTraceElement rootTrace = ExceptionUtils.getRootCause(e).getStackTrace()[0];
                String message = String.format("%s %s行空指针异常", rootTrace.getFileName(), rootTrace.getLineNumber());
                return Response.error(message);
            }
            return Response.error(e.getMessage());
        }
        return Response.error(DEFAULT_MESSAGE);
    }

    /**
     * 处理身份认证异常
     * redis 缓存没了
     *
     * @param e BizException
     * @return
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public ResponseEntity handleUnauthorized(BizException e) {
        logger.debug(e.getMessage(), e);
        return Response.unauthorized(e.getMessage());
    }


    /**
     * 处理上传限制异常
     *
     * @param e Exception
     * @return
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public ResponseEntity handleMaxUploadSizeExceededException(HttpServletRequest request,
                                                               MaxUploadSizeExceededException e) {
        long b = e.getMaxUploadSize();
        String size;
        if (b == 0) {
            size = "0B";
        } else if (b < 1024) {
            size = b +"B";
        } else if (b > 1024 && b < 1024 * 1024) {
            size = b / 1024 + "KB";
        } else {
            size = b / (1024 * 1024) + "MB";
        }
        String errorMsg = "上传的文件大小超过 " + size;
        String logErrorMsg = String.format("URL[%s] %s", request.getRequestURL(), errorMsg);
        logger.error(logErrorMsg, e);
        return Response.error(errorMsg);
    }

    private String getErrorMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : DEFAULT_MESSAGE;
    }

    /**
     * 是否携带详细信息
     * 只有在开发和测试环境才展示系统的错误信息
     *
     * @return
     */
    private boolean withDetail() {
        if (active == null) {
            return true;
        }
        for (String ignoredProfile : ignoredProfiles) {
            if (active.startsWith(ignoredProfile)) {
                return true;
            }
        }
        return false;
    }
}
