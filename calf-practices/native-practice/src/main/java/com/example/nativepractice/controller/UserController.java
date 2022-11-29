package com.example.nativepractice.controller;

import com.example.nativepractice.dto.UserCreateDTO;
import com.example.nativepractice.entity.User;
import com.example.nativepractice.service.UserService;
import com.example.nativepractice.util.BeanMaps;
import com.example.nativepractice.util.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xw
 * @date 2022/11/25
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(userService.list());
    }

    @PostMapping("/save")
    public Result<?> save(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        User user = BeanMaps.map(userCreateDTO, User.class);
        userService.save(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id) {
        userService.delete(id);
        return Result.success();
    }
}
