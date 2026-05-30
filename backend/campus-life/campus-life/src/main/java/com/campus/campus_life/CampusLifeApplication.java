package com.campus.campus_life;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.campus.campus_life.mapper")
public class CampusLifeApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusLifeApplication.class, args);
    }
}