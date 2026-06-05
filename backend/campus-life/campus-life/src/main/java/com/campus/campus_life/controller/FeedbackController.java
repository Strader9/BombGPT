package com.campus.campus_life.controller;

import com.campus.campus_life.entity.Feedback;
import com.campus.campus_life.service.FeedbackService;
import com.campus.campus_life.utils.AuthUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    // 客户端提交反馈
    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody Feedback feedback) {

        if (feedback.getContent() == null || feedback.getContent().trim().isEmpty()) {
            return Map.of(
                    "code", "400",
                    "msg", "反馈内容不能为空"
            );
        }

        feedbackService.submit(feedback);

        return Map.of(
                "code", "200",
                "msg", "反馈提交成功"
        );
    }

    // 管理端查看全部反馈：必须是管理员
    @GetMapping("/list")
    public Map<String, Object> listAll(HttpServletRequest request) {

        if (!AuthUtil.isAdmin(request)) {
            return Map.of(
                    "code", "403",
                    "msg", "无管理员权限"
            );
        }

        List<Feedback> list = feedbackService.listAll();

        return Map.of(
                "code", "200",
                "msg", "查询成功",
                "data", list
        );
    }

    // 客户端查看自己的反馈
    @GetMapping("/my")
    public Map<String, Object> listMine(@RequestParam String username) {

        List<Feedback> list = feedbackService.listByUsername(username);

        return Map.of(
                "code", "200",
                "msg", "查询成功",
                "data", list
        );
    }

    // 管理端回复反馈：必须是管理员
    @PostMapping("/reply")
    public Map<String, Object> reply(
            @RequestBody Map<String, Object> map,
            HttpServletRequest request
    ) {

        if (!AuthUtil.isAdmin(request)) {
            return Map.of(
                    "code", "403",
                    "msg", "无管理员权限"
            );
        }

        Long id = Long.valueOf(map.get("id").toString());
        String reply = map.get("reply").toString();

        if (reply == null || reply.trim().isEmpty()) {
            return Map.of(
                    "code", "400",
                    "msg", "回复内容不能为空"
            );
        }

        feedbackService.reply(id, reply);

        return Map.of(
                "code", "200",
                "msg", "回复成功"
        );
    }
}