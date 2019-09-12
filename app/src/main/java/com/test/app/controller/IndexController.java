package com.test.app.controller;

import com.test.app.common.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

/**
 * @author Shoven
 * @date 2019-03-21 16:03
 */
@Controller
public class IndexController {

    @GetMapping(value = "/", produces = "text/html")
    public String indexHtml() throws IOException {
        return "index";
    }

    @GetMapping("/")
    public ResponseEntity indexBody() {
        return Response.ok("hello world");
    }


}
