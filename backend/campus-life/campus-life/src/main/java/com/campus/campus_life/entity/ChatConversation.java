package com.campus.campus_life.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatConversation {

    private Long id;

    private String username;

    private String title;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}