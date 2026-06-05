package com.campus.campus_life.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Feedback {

    private Long id;

    private Long userId;

    private String username;

    private String content;

    private String reply;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime replyTime;
}