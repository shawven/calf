package com.test.controller;

import com.test.common.Response;
import com.test.domain.User;
import com.test.support.log.annotation.Log;
import com.test.support.log.annotation.LogArg;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Shoven
 * @date 2019-03-21 16:03
 */
@Log(module = "首页控制器")
@RestController
public class IndexController {

    @RequestMapping
    public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Log("testObjects操作")
    @PostMapping("objects")
    public ResponseEntity testObjects(@LogArg("users") @RequestBody List<User> users2) {
        return Response.ok();
    }

    @DeleteMapping("{id}")
    ResponseEntity delete(@PathVariable(value = "id") String id) {
        return Response.noContent();
    }
}
