package com.campus.campus_life.mapper;

import com.campus.campus_life.entity.Feedback;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FeedbackMapper {

    // 用户提交反馈
    @Insert("""
        INSERT INTO feedback(username, content, status, create_time)
        VALUES(#{username}, #{content}, 'PENDING', NOW())
    """)
    void insert(Feedback feedback);

    // 管理员查看全部反馈
    @Select("""
        SELECT *
        FROM feedback
        ORDER BY create_time DESC
    """)
    List<Feedback> listAll();

    // 用户查看自己的反馈
    @Select("""
        SELECT *
        FROM feedback
        WHERE username = #{username}
        ORDER BY create_time DESC
    """)
    List<Feedback> listByUsername(String username);

    // 管理员回复反馈
    @Update("""
        UPDATE feedback
        SET reply = #{reply},
            status = 'REPLIED',
            reply_time = NOW()
        WHERE id = #{id}
    """)
    void reply(@Param("id") Long id, @Param("reply") String reply);
}