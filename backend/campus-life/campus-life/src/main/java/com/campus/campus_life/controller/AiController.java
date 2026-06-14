package com.campus.campus_life.controller;

import com.campus.campus_life.service.AiService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiService aiService;

    /**
     * 用户提交问题，返回 AI 回答。
     *
     * 返回字段说明：
     * answer：AI 回复正文，兼容你原来的前端代码
     * sourceType：回答来源类型，给前端做样式判断
     * sourceLabel：回答来源小标签，显示在 AI 气泡右下角
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> map) {
        String question = map.getOrDefault("question", "");

        Map<String, Object> result = new LinkedHashMap<>();

        if (question == null || question.trim().isEmpty()) {
            result.put("code", "400");
            result.put("msg", "问题不能为空");
            result.put("answer", "请先输入你的问题。");
            result.put("sourceType", "SYSTEM");
            result.put("sourceLabel", "系统提示");
            return result;
        }

        try {
            String answer = aiService.chat(question);

            result.put("code", "200");
            result.put("msg", "success");
            result.put("answer", answer);

            /*
             * 注意：
             * 目前 AiService.chat() 只返回 String，所以 Controller 还不能百分百知道
             * 这次回答到底是“知识库命中”还是“AI 谨慎回答”。
             *
             * 这里先返回一个通用来源标签，前端可以先显示。
             * 下一步如果你想精确显示：
             * - AI 整理自校园知识库
             * - AI 谨慎回答
             * - 系统提示
             * 我们需要把 AiService.chat() 改成返回对象。
             */
            result.put("sourceType", detectSourceType(answer));
            result.put("sourceLabel", buildSourceLabel(answer));

            return result;

        } catch (Exception e) {
            e.printStackTrace();

            result.put("code", "500");
            result.put("msg", "AI服务异常");
            result.put("answer", "AI服务暂时不可用，请稍后再试。");
            result.put("sourceType", "SYSTEM");
            result.put("sourceLabel", "系统提示");
            return result;
        }
    }

    /**
     * 根据回答内容粗略判断来源类型。
     * 这是兼容当前 AiService 只返回 String 的过渡方案。
     */
    private String detectSourceType(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return "SYSTEM";
        }

        String text = answer.trim();

        if (text.contains("目前系统还没有同步你的个人教务信息")
                || text.contains("你好，我是 BBG 校园生活百事通")
                || text.contains("请先输入你的问题")
                || text.contains("AI模型调用失败")
                || text.contains("AI服务暂时不可用")
                || text.contains("系统自检结果如下")) {
            return "SYSTEM";
        }

        if (text.contains("知识库暂无相关信息")
                || text.contains("无法确定")
                || text.contains("建议查看学校官网")
                || text.contains("建议补充具体校区")) {
            return "AI_CAUTIOUS";
        }

        return "AI_KNOWLEDGE";
    }

    /**
     * 构造前端显示的小标签。
     */
    private String buildSourceLabel(String answer) {
        String sourceType = detectSourceType(answer);

        if ("SYSTEM".equals(sourceType)) {
            return "系统提示";
        }

        if ("AI_CAUTIOUS".equals(sourceType)) {
            return "AI 谨慎回答";
        }

        return "AI 整理自校园知识库";
    }
}