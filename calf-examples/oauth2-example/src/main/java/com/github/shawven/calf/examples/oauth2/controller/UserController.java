package com.github.shawven.calf.examples.oauth2.controller;


import com.github.shawven.calf.examples.oauth2.domain.UserProfileDTO;
import com.github.shawven.calf.examples.oauth2.domain.UserRegisterRequest;
import com.github.shawven.calf.examples.oauth2.domain.UserUpdateRequest;
import com.github.shawven.calf.examples.oauth2.service.UserService;
import com.github.shawven.calf.examples.oauth2.support.Context;
import com.github.shawven.calf.examples.oauth2.support.Response;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.regex.Pattern;

/**
 * <p>
 * 操作员表 前端控制器
 * </p>
 *
 * @author Shoven
 * @date 2019-10-26
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private Context ctx;

    @GetMapping("me")
    public ResponseEntity getUserProfile() {
        UserProfileDTO profile = userService.getUserProfile(ctx.getUserId());
        return Response.ok("获取用户信息成功", profile);
    }

    @PutMapping("me")
    public ResponseEntity updateUserProfile(UserUpdateRequest request) {
        request.setId(ctx.getUserId());
        userService.updateUser(request);
        return Response.ok("更新用户信息成功");
    }

    @PostMapping("phone")
    public ResponseEntity updatePhone(String phone) {
        if (!Pattern.matches("1[3-9][0-9]{9}", phone)) {
            return Response.badRequest("手机号格式不正确");
        }
        userService.updatePhone(ctx.getUserId(), phone);
        return Response.ok("手机号修改成功");
    }

    @PostMapping("password")
    public ResponseEntity resetPassword(String phone, String password) {
        if (StringUtils.isBlank(password)) {
            return Response.badRequest("密码不能为空");
        }
        password = password.trim();
        if (password.length() < 6) {
            return Response.badRequest("密码不能小于6位数");
        }
        userService.resetPassword(phone, password);
        return Response.ok("密码修改成功");
    }

    @PostMapping("register")
    public ResponseEntity registerUser(@Valid UserRegisterRequest request) {
        userService.register(request);
        return Response.ok("用户注册成功");
    }
}
