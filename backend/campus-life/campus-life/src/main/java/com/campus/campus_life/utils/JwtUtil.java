package com.campus.campus_life.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    // 🔥 关键：使用标准 HMAC-SHA256 密钥，绝对不会报错
    private static final String SECRET_KEY = "mySecretKey123456789012345678901234";
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 生成密钥对象
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // 生成 Token
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1天
                .signWith(getSigningKey()) // 🔥 修复点
                .compact();
    }

    // 解析Token（拦截器里要用到的关键方法）
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}