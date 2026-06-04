package com.campus.campus_life.service;

public interface AiService {

    /**
     * 根据用户问题返回AI回答
     * @param question 用户问题
     * @return AI回答
     */
    String chat(String question);
}