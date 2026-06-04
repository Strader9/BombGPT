package com.campus.campus_life.service.impl;

import com.campus.campus_life.entity.Knowledge;
import com.campus.campus_life.mapper.KnowledgeMapper;
import com.campus.campus_life.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    @Value("${ai.url}")
    private String aiUrl;

    @Value("${ai.model}")
    private String aiModel;

    private final RestTemplate restTemplate = new RestTemplate();

    @Resource
    private KnowledgeMapper knowledgeMapper; // 注入数据库 Mapper

    @Override
    public String chat(String question) {
        try {
            // 1. 先从数据库取知识库相关内容
            List<Knowledge> knowledgeList = knowledgeMapper.list(); // 全量获取，后面可优化为关键词匹配

            String knowledgeText = knowledgeList.stream()
                    .map(k -> "问题：" + k.getQuestion() + "\n答案：" + k.getAnswer())
                    .collect(Collectors.joining("\n\n"));

            // 2. 组装发送给 Ollama 的请求体
            Map<String, Object> requestBody = Map.of(
                    "model", aiModel,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "你是校园生活百事通智能助手。请用中文回答用户的问题，回答要简洁、准确、像客服一样自然。优先参考提供的知识库回答，如果知识库没有相关内容，再自行回答。"
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", "知识库内容:\n" + knowledgeText + "\n用户问题:\n" + question
                            )
                    ),
                    "stream", false
            );

            // 3. 发送 POST 请求给本地 Ollama
            Map response = restTemplate.postForObject(aiUrl, requestBody, Map.class);

            // 4. 解析 Ollama 返回结果
            if (response == null) {
                return "AI模型没有返回内容，请检查 Ollama 是否启动。";
            }

            List choices = (List) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "AI模型返回格式异常，请检查模型名称是否正确。";
            }

            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");
            if (message == null) {
                return "AI模型返回内容为空。";
            }

            Object content = message.get("content");
            if (content == null) {
                return "AI没有生成有效回答。";
            }

            return content.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "AI模型调用失败，请检查 Ollama 是否已启动，以及模型名称是否正确。";
        }
    }
}