package com.campus.campus_life.service.impl;

import com.campus.campus_life.entity.ChatConversation;
import com.campus.campus_life.entity.ChatMessage;
import com.campus.campus_life.mapper.ChatMemoryMapper;
import com.campus.campus_life.service.ChatMemoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMemoryServiceImpl implements ChatMemoryService {

    @Resource
    private ChatMemoryMapper chatMemoryMapper;

    @Override
    public ChatConversation createConversation(String username, String title) {
        ChatConversation conversation = new ChatConversation();
        conversation.setUsername(username);
        conversation.setTitle(title == null || title.trim().isEmpty() ? "新对话" : title.trim());

        chatMemoryMapper.createConversation(conversation);

        return conversation;
    }

    @Override
    public List<ChatConversation> listConversation(String username) {
        return chatMemoryMapper.listConversation(username);
    }

    @Override
    public List<ChatMessage> listMessage(Long conversationId, String username) {
        return chatMemoryMapper.listMessage(conversationId, username);
    }

    @Override
    public void saveMessage(ChatMessage message) {
        chatMemoryMapper.insertMessage(message);
        chatMemoryMapper.updateConversationTime(message.getConversationId(), message.getUsername());
    }

    @Override
    public void updateTitle(Long id, String username, String title) {
        chatMemoryMapper.updateConversationTitle(id, username, title);
    }

    @Override
    public void deleteConversation(Long id, String username) {
        chatMemoryMapper.deleteConversation(id, username);
    }
}