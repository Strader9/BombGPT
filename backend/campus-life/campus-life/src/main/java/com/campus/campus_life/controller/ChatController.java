package com.campus.campus_life.controller;

import com.campus.campus_life.entity.Knowledge;
import com.campus.campus_life.mapper.KnowledgeMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final KnowledgeMapper km;

    public ChatController(KnowledgeMapper km) {
        this.km = km;
    }

    @PostMapping("/ask")
    public Map<String, Object> ask(@RequestBody Map<String,String> map) {
        String q = map.get("question");
        List<Knowledge> list = km.search(q);

        if (list.isEmpty()) {
            return Map.of("answer","抱歉，知识库暂无相关答案。");
        }

        StringBuilder sb = new StringBuilder();
        for (Knowledge k : list) {
            sb.append("• ").append(k.getQuestion()).append("：")
                    .append(k.getAnswer()).append("\n\n");
        }
        return Map.of("answer", sb.toString());
    }
}