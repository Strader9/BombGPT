package com.campus.campus_life.service;

public interface AiService {
    /**
     * 增强版：优先使用数据库回答，如果数据库未命中，则调用 Ollama 生成自然语言回答
     * @param question 用户问题
     * @return 回答内容
     */
    String chat(String question);
}