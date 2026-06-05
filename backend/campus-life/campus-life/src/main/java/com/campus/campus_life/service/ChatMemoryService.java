package com.campus.campus_life.service;

import com.campus.campus_life.entity.ChatConversation;
import com.campus.campus_life.entity.ChatMessage;

import java.util.List;

public interface ChatMemoryService {

    ChatConversation createConversation(String username, String title);

    List<ChatConversation> listConversation(String username);

    List<ChatMessage> listMessage(Long conversationId, String username);

    void saveMessage(ChatMessage message);

    void updateTitle(Long id, String username, String title);

    void deleteConversation(Long id, String username);
}