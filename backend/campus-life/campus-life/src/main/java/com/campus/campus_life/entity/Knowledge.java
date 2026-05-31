package com.campus.campus_life.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Knowledge {
    private Long id;
    private String question;  // 对应数据库
    private String answer;    // 对应数据库
    private String keywords;  // 对应数据库
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}