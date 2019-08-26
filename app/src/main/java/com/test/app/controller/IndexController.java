package com.test.app.controller;

import com.test.app.common.Response;
import com.test.app.domain.User;
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
@RestController
public class IndexController {

    @RequestMapping
    public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @PostMapping("objects")
    public ResponseEntity testObjects(@RequestBody List<User> users2) {
        return Response.ok();
    }

    @DeleteMapping("{id}")
    ResponseEntity delete(@PathVariable(value = "id") String id) {
        return Response.noContent();
    }
}
