package com.campus.campus_life.service;

import com.campus.campus_life.entity.User;
import com.campus.campus_life.mapper.CodeMapper;
import com.campus.campus_life.mapper.UserMapper;
import com.campus.campus_life.utils.JwtUtil;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final CodeMapper codeMapper;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public UserService(UserMapper um, CodeMapper cm, JwtUtil ju, EmailService es) {
        this.userMapper = um;
        this.codeMapper = cm;
        this.jwtUtil = ju;
        this.emailService = es;
    }

    // 登录（开发阶段临时用，直接字符串对比）
    public Map<String, Object> login(String username, String password) {
        User user = userMapper.findByUsername(username);
        Map<String, Object> res = new HashMap<>();

        // 直接对比明文密码（仅开发用！）
        if (user == null || !password.equals(user.getPassword())) {
            res.put("code", 400);
            res.put("msg", "账号或密码错误");
            return res;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        String token = jwtUtil.generateToken(claims);

        res.put("code", 200);
        res.put("token", token);
        res.put("role", user.getRole());
        return res;
    }

    // 注册（开发阶段临时用，直接存明文密码）
    public Map<String, Object> register(String username, String email, String password, String adminCode) {
        Map<String, Object> res = new HashMap<>();
        if (userMapper.findByUsername(username) != null) {
            res.put("code", 400); res.put("msg", "用户名已存在"); return res;
        }
        if (userMapper.findByEmail(email) != null) {
            res.put("code", 400); res.put("msg", "邮箱已注册"); return res;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        // 直接存明文密码（仅开发用！）
        user.setPassword(password);

        if ("ADMIN_2025".equals(adminCode)) {
            user.setRole("ADMIN");
        } else {
            user.setRole("USER");
        }

        userMapper.insert(user);
        res.put("code", 200);
        res.put("msg", "注册成功");
        return res;
    }

    public void sendCode(String email) throws Exception {
        String code = String.format("%06d", new Random().nextInt(999999));
        codeMapper.insert(email, code, java.time.LocalDateTime.now().plusMinutes(5));
        emailService.sendCode(email, code);
    }

    public Map<String, Object> resetPassword(String email, String code, String newPwd) {
        Map<String, Object> res = new HashMap<>();
        if (codeMapper.check(email, code) == 0) {
            res.put("code", 400); res.put("msg", "验证码错误或过期"); return res;
        }
        // 直接更新明文密码（仅开发用！）
        userMapper.updatePassword(email, newPwd);
        res.put("code", 200); res.put("msg", "重置成功");
        return res;
    }
}