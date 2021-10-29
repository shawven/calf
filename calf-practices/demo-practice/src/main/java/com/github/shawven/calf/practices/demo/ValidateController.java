package com.github.shawven.calf.practices.demo;

import com.github.shawven.calf.practices.demo.support.Response;
import com.github.shawven.calf.practices.demo.support.SearchQueryRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author xw
 * @date 2021/10/12
 */
@RestController
@RequestMapping("validate")
public class ValidateController {

    @GetMapping("query")
    public ResponseEntity<?> testQueryParam(@Valid SearchQueryRequest request) {
        return Response.ok();
    }

    @PostMapping("body")
    public ResponseEntity<?> testBody(@Valid @RequestBody SearchQueryRequest request) {
        return Response.ok();
    }

//    @GetMapping("method")
//    @Valid
//    public ResponseEntity<?> testMethod1(SearchQueryRequest request) {
//        return Response.ok();
//    }
//
//    @GetMapping("method2")
//    public ResponseEntity<?> testMethod2(@Valid @NotBlank(message = "名词不能为空") String name) {
//        return Response.ok();
//    }
}
