package com.campus.campus_life.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {

    private Long id;

    private Long conversationId;

    private String username;

    private String role;

    private String content;

    private LocalDateTime createTime;
}