package com.campus.campus_life;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// 关键：加上 exclude 禁用 Security 自动配置
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("com.campus.campus_life.mapper")
public class CampusLifeApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusLifeApplication.class, args);
    }
}

