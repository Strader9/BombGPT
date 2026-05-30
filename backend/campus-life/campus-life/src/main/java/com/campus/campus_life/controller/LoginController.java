package com.campus.campus_life.controller;

import com.campus.campus_life.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        // 真实账号密码
        if ("admin".equals(user.getUsername()) && "123456".equals(user.getPassword())) {
            result.put("code", 200);
            result.put("token", "login-success-token");
            result.put("msg", "登录成功");
        } else {
            result.put("code", 400);
            result.put("msg", "用户名或密码错误");
        }
        return result;
    }
}