package com.campus.campus_life.service.impl;


import com.campus.campus_life.entity.Knowledge;
import com.campus.campus_life.mapper.KnowledgeMapper;
import com.campus.campus_life.service.AiService;
import com.campus.campus_life.utils.OllamaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    @Autowired
    private OllamaClient ollamaClient;

    @Override
    public String chat(String question) {
        // 1. 先从数据库检索知识库
        List<Knowledge> list = knowledgeMapper.search(question);

        // 2. 如果知识库为空，直接返回提示
        if (list.isEmpty()) {
            return "数据库中没有相关内容";
        }

        // 3. 拼接知识库内容
        String knowledge = list.stream()
                // 注意：如果你的实体类里字段是 answer，这里要改成 getAnswer()
                .map(Knowledge::getAnswer)
                .collect(Collectors.joining("\n"));

        // 4. 构建提示词
        String prompt = """
                你是校园百事通。
                
                仅根据以下知识库回答：
                %s
                
                用户问题：
                %s
                
                如果知识库没有答案，
                请回答：
                没有找到答案
                """.formatted(knowledge, question);

        // 5. 调用本地 Ollama
        return ollamaClient.chat(prompt);
    }
}
