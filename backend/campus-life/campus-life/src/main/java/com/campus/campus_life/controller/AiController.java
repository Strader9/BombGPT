package com.campus.campus_life.controller;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import com.campus.campus_life.service.AiService;
import java.util.Map;


@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiService aiService;

    @PostMapping("/chat")
    public String chat(@RequestBody Map<String,String> map){

        String question = map.get("question");

        return aiService.chat(question);
    }
}