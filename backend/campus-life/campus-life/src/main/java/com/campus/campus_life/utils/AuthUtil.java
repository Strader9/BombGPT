package com.campus.campus_life.utils;

import jakarta.servlet.http.HttpServletRequest;

public class AuthUtil {
    // 判断是否是管理员
    public static boolean isAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        return "ADMIN".equals(role);
    }
}
