package com.campus.campus_life.controller;

import com.campus.campus_life.entity.ChatConversation;
import com.campus.campus_life.entity.ChatMessage;
import com.campus.campus_life.service.ChatMemoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/memory")
public class ChatMemoryController {

    @Resource
    private ChatMemoryService chatMemoryService;

    // 创建新对话
    @PostMapping("/conversation/create")
    public Map<String, Object> createConversation(@RequestBody Map<String, Object> map) {
        String username = String.valueOf(map.get("username"));
        String title = map.get("title") == null ? "新对话" : String.valueOf(map.get("title"));

        if (username == null || username.trim().isEmpty() || "null".equals(username)) {
            return Map.of(
                    "code", "400",
                    "msg", "用户名不能为空"
            );
        }

        ChatConversation conversation = chatMemoryService.createConversation(username, title);

        return Map.of(
                "code", "200",
                "msg", "创建成功",
                "data", conversation
        );
    }

    // 查询用户历史对话
    @GetMapping("/conversation/list")
    public Map<String, Object> listConversation(@RequestParam String username) {
        List<ChatConversation> list = chatMemoryService.listConversation(username);

        return Map.of(
                "code", "200",
                "msg", "查询成功",
                "data", list
        );
    }

    // 修改对话标题
    @PostMapping("/conversation/update-title")
    public Map<String, Object> updateTitle(@RequestBody Map<String, Object> map) {
        Long id = Long.valueOf(map.get("id").toString());
        String username = String.valueOf(map.get("username"));
        String title = String.valueOf(map.get("title"));

        if (title == null || title.trim().isEmpty() || "null".equals(title)) {
            return Map.of(
                    "code", "400",
                    "msg", "标题不能为空"
            );
        }

        chatMemoryService.updateTitle(id, username, title);

        return Map.of(
                "code", "200",
                "msg", "修改成功"
        );
    }

    // 删除对话
    @PostMapping("/conversation/delete")
    public Map<String, Object> deleteConversation(@RequestBody Map<String, Object> map) {
        Long id = Long.valueOf(map.get("id").toString());
        String username = String.valueOf(map.get("username"));

        chatMemoryService.deleteConversation(id, username);

        return Map.of(
                "code", "200",
                "msg", "删除成功"
        );
    }

    // 保存聊天消息
    @PostMapping("/message/save")
    public Map<String, Object> saveMessage(@RequestBody ChatMessage message) {
        if (message.getConversationId() == null) {
            return Map.of(
                    "code", "400",
                    "msg", "conversationId不能为空"
            );
        }

        if (message.getUsername() == null || message.getUsername().trim().isEmpty()) {
            return Map.of(
                    "code", "400",
                    "msg", "username不能为空"
            );
        }

        if (message.getRole() == null || message.getRole().trim().isEmpty()) {
            return Map.of(
                    "code", "400",
                    "msg", "role不能为空"
            );
        }

        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return Map.of(
                    "code", "400",
                    "msg", "content不能为空"
            );
        }

        chatMemoryService.saveMessage(message);

        return Map.of(
                "code", "200",
                "msg", "保存成功"
        );
    }

    // 查询某个对话的消息
    @GetMapping("/message/list")
    public Map<String, Object> listMessage(
            @RequestParam Long conversationId,
            @RequestParam String username
    ) {
        List<ChatMessage> list = chatMemoryService.listMessage(conversationId, username);

        return Map.of(
                "code", "200",
                "msg", "查询成功",
                "data", list
        );
    }
}