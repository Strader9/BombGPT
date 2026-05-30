package com.campus.campus_life.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Category {

    private Long id;

    private String name;

    private String icon;

    private Integer sortOrder;

    private LocalDateTime createTime;
}