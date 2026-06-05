package com.campus.campus_life.mapper;

import com.campus.campus_life.entity.ChatConversation;
import com.campus.campus_life.entity.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatMemoryMapper {

    // 创建新对话
    @Insert("""
        INSERT INTO chat_conversation(username, title, deleted, create_time, update_time)
        VALUES(#{username}, #{title}, 0, NOW(), NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void createConversation(ChatConversation conversation);

    // 查询某个用户的历史对话
    @Select("""
        SELECT *
        FROM chat_conversation
        WHERE username = #{username}
          AND deleted = 0
        ORDER BY update_time DESC
    """)
    List<ChatConversation> listConversation(@Param("username") String username);

    // 修改对话标题
    @Update("""
        UPDATE chat_conversation
        SET title = #{title},
            update_time = NOW()
        WHERE id = #{id}
          AND username = #{username}
    """)
    void updateConversationTitle(
            @Param("id") Long id,
            @Param("username") String username,
            @Param("title") String title
    );

    // 软删除对话
    @Update("""
        UPDATE chat_conversation
        SET deleted = 1,
            update_time = NOW()
        WHERE id = #{id}
          AND username = #{username}
    """)
    void deleteConversation(
            @Param("id") Long id,
            @Param("username") String username
    );

    // 保存一条聊天消息
    @Insert("""
        INSERT INTO chat_message(conversation_id, username, role, content, create_time)
        VALUES(#{conversationId}, #{username}, #{role}, #{content}, NOW())
    """)
    void insertMessage(ChatMessage message);

    // 查询某个对话的全部消息
    @Select("""
        SELECT *
        FROM chat_message
        WHERE conversation_id = #{conversationId}
          AND username = #{username}
        ORDER BY create_time ASC, id ASC
    """)
    List<ChatMessage> listMessage(
            @Param("conversationId") Long conversationId,
            @Param("username") String username
    );

    // 每次保存消息后，更新对话时间
    @Update("""
        UPDATE chat_conversation
        SET update_time = NOW()
        WHERE id = #{conversationId}
          AND username = #{username}
    """)
    void updateConversationTime(
            @Param("conversationId") Long conversationId,
            @Param("username") String username
    );
}