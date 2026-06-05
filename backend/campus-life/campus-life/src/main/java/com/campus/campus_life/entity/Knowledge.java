package com.campus.campus_life.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Knowledge {

    private Long id;

    private Long categoryId;

    private String question;

    private String answer;

    private String keywords;

    private Integer viewCount;

    private Integer sourceType;

    private Integer status;

    private String contributor;

    private String adminNote;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}