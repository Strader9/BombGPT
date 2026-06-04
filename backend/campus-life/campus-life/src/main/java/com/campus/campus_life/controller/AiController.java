package com.campus.campus_life.controller;

import com.campus.campus_life.service.AiService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiService aiService;

    // 用户提交问题，返回AI回答
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String,String> map){
        String question = map.get("question");
        String answer = aiService.chat(question);
        return Map.of("answer", answer);
    }
}