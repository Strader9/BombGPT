package com.campus.campus_life.controller;

import com.campus.campus_life.entity.User;
import com.campus.campus_life.mapper.UserMapper;
import com.campus.campus_life.service.UserService;
import com.campus.campus_life.utils.JwtUtil;
import com.campus.campus_life.utils.AuthUtil;
import com.campus.campus_life.utils.Result;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService,
                          UserMapper userMapper,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    // ====================== 登录修复（核心） ======================
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        // 1. 查用户
        User user = userMapper.findByUsername(username);
        if (user == null || !password.equals(user.getPassword())) {
            return Result.error("账号或密码错误");
        }

        // 2. 生成token（包含角色）
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        String token = jwtUtil.generateToken(claims);

        // 3. 封装返回数据（必须带 role！！！）
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("role", user.getRole());
        data.put("username", user.getUsername());

        // 4. 统一返回（前端靠这个跳转、显示管理按钮）
        return Result.success(data);
    }

    // ====================== 以下保持你原来的代码不动 ======================
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String,String> map) {
        return userService.register(
                map.get("username"),
                map.get("email"),
                map.get("password"),
                map.get("adminCode")
        );
    }

    @PostMapping("/send-code")
    public Map<String, Object> sendCode(@RequestBody Map<String,String> map) throws Exception {
        userService.sendCode(map.get("email"));
        return Map.of("code",200,"msg","发送成功");
    }

    @PostMapping("/reset-pwd")
    public Map<String, Object> resetPwd(@RequestBody Map<String,String> map) {
        return userService.resetPassword(
                map.get("email"),
                map.get("code"),
                map.get("password")
        );
    }

    @GetMapping("/admin/dashboard")
    public Result adminDashboard(HttpServletRequest request) {
        if (!AuthUtil.isAdmin(request)) {
            return Result.error("无管理员权限");
        }
        return Result.success("管理员控制台数据");
    }

    @GetMapping("/user/profile")
    public Result userProfile(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return Result.success("当前登录用户：" + username);
    }
}