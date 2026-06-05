package com.campus.campus_life.service.impl;

import com.campus.campus_life.entity.Knowledge;
import com.campus.campus_life.mapper.KnowledgeMapper;
import com.campus.campus_life.service.AiService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    private KnowledgeMapper knowledgeMapper;

    @Override
    public String chat(String question) {
        try {
            // 1. 先根据用户问题检索相关知识
            List<Knowledge> knowledgeList = knowledgeMapper.searchForAi(question);

            // 2. 如果没有检索到，就取少量热门启用知识兜底
            if (knowledgeList == null || knowledgeList.isEmpty()) {
                knowledgeList = knowledgeMapper.listEnabledLimit();
            }

            // 3. 拼接知识库内容
            String knowledgeText = knowledgeList.stream()
                    .map(k -> "问题：" + k.getQuestion() + "\n答案：" + k.getAnswer())
                    .collect(Collectors.joining("\n\n"));

            // 4. 组装请求 Ollama 的内容
            Map<String, Object> requestBody = Map.of(
                    "model", aiModel,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "你是校园生活百事通智能助手。请用中文回答用户的问题，回答要简洁、准确、自然。优先参考提供的知识库内容。如果知识库内容和用户问题无关，不要强行编造，请礼貌说明知识库暂无相关信息。"
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", "知识库内容：\n" + knowledgeText + "\n\n用户问题：\n" + question
                            )
                    ),
                    "stream", false
            );

            Map response = restTemplate.postForObject(aiUrl, requestBody, Map.class);

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