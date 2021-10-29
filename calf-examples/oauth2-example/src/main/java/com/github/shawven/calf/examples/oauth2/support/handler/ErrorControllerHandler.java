package com.github.shawven.calf.examples.oauth2.support.handler;

import com.github.shawven.calf.examples.oauth2.support.Response;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-04-22 20:03
 */
@Controller
public class ErrorControllerHandler extends BasicErrorController {


    public ErrorControllerHandler(ErrorAttributes errorAttributes,
                                  ServerProperties serverProperties,
                                  List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, serverProperties.getError(), errorViewResolvers);
    }

    /**
     * 错误处理方法
     *
     * @param request
     * @return
     */
    @Override
    @RequestMapping
    public ResponseEntity error(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.ALL));
        HttpStatus status = getStatus(request);
        return Response.build(status, String.valueOf(body.get("message")));
    }
}
