package com.example.nativepractice.framework;

import com.example.nativepractice.util.Result;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 * @author TanZhen
 * @since 2018年8月24日 下午6:46:29
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private final static String PARAM_ERROR = "参数有误";

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseBody
	public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 获取错误字段集合
        String msg = buildErrorTips(e.getBindingResult());
        logger.debug("handleMethodArgumentNotValidException: " + msg, e);
		return Result.error(msg);
	}

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public Result<Object> handleBindException(BindException e) {
        // 获取错误字段集合
        String msg = buildErrorTips(e.getBindingResult());
        logger.debug("handleBindException: " + msg, e);
        return Result.error(msg);
    }

    private String buildErrorTips(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String msg = fieldErrors.stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(f -> "[" + f.getField() + "]" + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        msg = fieldErrors.size() > 0 ? msg : PARAM_ERROR;
        return msg;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public Result<Object> handleConstraintViolationException(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .sorted(Comparator.comparing(c -> c.getPropertyPath().toString()))
                .map(c -> "[" + c.getPropertyPath().toString() + "]" + c.getMessage())
                .collect(Collectors.joining(", "));
        logger.debug("handleConstraintViolationException: " + msg, e);
        return Result.error(msg);
    }

    /**
     * HTTP方法不匹配
     *
     * @param e MethodNotSupportedException
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public Result<Object> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.debug("handleMethodNotSupportedException: " + e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    /**
     * json解析失败
     *
     * @param e
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public Result<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        if (cause instanceof MismatchedInputException) {
            MismatchedInputException exception = (MismatchedInputException) cause;
            String error = exception.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            if (StringUtils.isNotBlank(error)) {
                logger.warn(e.getMessage(), e);
                String msg = PARAM_ERROR +"[" + error +"]";
                return Result.error(msg);
            }
        }
        logger.warn("handleHttpMessageNotReadableException: " + e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    /**
     * 参数缺少
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public Result<Object> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String msg;
        if (StringUtils.contains(e.getMessage(), "is not present")) {
            msg = "参数[" + e.getParameterName() + "]" +  "缺失";
        } else {
            msg = e.getMessage();
        }
        logger.warn("handleMissingServletRequestParameterException: " + msg, e);
        return Result.error(msg);
    }

}
