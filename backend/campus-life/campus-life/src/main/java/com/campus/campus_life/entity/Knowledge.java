package com.campus.campus_life.entity;

import lombok.Data;

@Data
public class Knowledge {
    private Long id;
    private String title;
    private String content;
    private Long categoryId; // 关联分类表的外键
    private Integer viewCount; // 浏览量
}