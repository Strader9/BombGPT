package com.campus.campus_life.mapper;

import com.campus.campus_life.entity.Knowledge;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KnowledgeMapper {

    // 查询全部知识，管理端使用
    @Select("""
        SELECT *
        FROM knowledge
        ORDER BY id DESC
    """)
    List<Knowledge> list();

    // 客户端按分类查询，只查启用内容
    @Select("""
        SELECT *
        FROM knowledge
        WHERE category_id = #{categoryId}
          AND status = 1
        ORDER BY id DESC
    """)
    List<Knowledge> selectByCategoryId(@Param("categoryId") Long categoryId);

    // 客户端搜索，只查启用内容
    @Select("""
        SELECT *
        FROM knowledge
        WHERE status = 1
          AND (
              question LIKE CONCAT('%', #{keyword}, '%')
              OR answer LIKE CONCAT('%', #{keyword}, '%')
              OR keywords LIKE CONCAT('%', #{keyword}, '%')
          )
        ORDER BY id DESC
    """)
    List<Knowledge> search(@Param("keyword") String keyword);

    // 根据ID查询详情
    @Select("""
        SELECT *
        FROM knowledge
        WHERE id = #{id}
    """)
    Knowledge selectById(@Param("id") Long id);

    // 热门TOP10，只查启用内容
    @Select("""
        SELECT *
        FROM knowledge
        WHERE status = 1
        ORDER BY view_count DESC
        LIMIT 10
    """)
    List<Knowledge> listHot();

    // 阅读数+1
    @Update("""
        UPDATE knowledge
        SET view_count = view_count + 1
        WHERE id = #{id}
    """)
    void increaseViewCount(@Param("id") Long id);

    // AI专用：按用户问题检索相关知识，只查启用内容
    @Select("""
        SELECT *
        FROM knowledge
        WHERE status = 1
          AND (
              question LIKE CONCAT('%', #{keyword}, '%')
              OR answer LIKE CONCAT('%', #{keyword}, '%')
              OR keywords LIKE CONCAT('%', #{keyword}, '%')
              OR #{keyword} LIKE CONCAT('%', question, '%')
          )
        ORDER BY view_count DESC, update_time DESC
        LIMIT 10
    """)
    List<Knowledge> searchForAi(@Param("keyword") String keyword);

    // AI专用：如果没有搜到相关知识，就取部分热门知识兜底
    @Select("""
        SELECT *
        FROM knowledge
        WHERE status = 1
        ORDER BY view_count DESC, update_time DESC
        LIMIT 30
    """)
    List<Knowledge> listEnabledLimit();

    // 管理员新增知识
    @Insert("""
        INSERT INTO knowledge(
            category_id,
            question,
            answer,
            keywords,
            view_count,
            source_type,
            status,
            contributor,
            admin_note,
            create_time,
            update_time
        )
        VALUES(
            #{categoryId},
            #{question},
            #{answer},
            #{keywords},
            0,
            1,
            1,
            #{contributor},
            #{adminNote},
            NOW(),
            NOW()
        )
    """)
    void insert(Knowledge knowledge);

    // 管理员修改知识
    @Update("""
        UPDATE knowledge
        SET
            category_id = #{categoryId},
            question = #{question},
            answer = #{answer},
            keywords = #{keywords},
            status = #{status},
            admin_note = #{adminNote},
            update_time = NOW()
        WHERE id = #{id}
    """)
    void update(Knowledge knowledge);

    // 管理员删除知识
    @Delete("""
        DELETE FROM knowledge
        WHERE id = #{id}
    """)
    void deleteById(@Param("id") Long id);
}