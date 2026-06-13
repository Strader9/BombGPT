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

    // 管理端：分页查询知识库
    @Select("""
        SELECT *
        FROM knowledge
        ORDER BY id DESC
        LIMIT #{offset}, #{pageSize}
    """)
    List<Knowledge> listPage(
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    // 管理端：统计知识库总数
    @Select("""
        SELECT COUNT(*)
        FROM knowledge
    """)
    int countAll();

    // 统计启用知识数量
    @Select("""
        SELECT COUNT(*)
        FROM knowledge
        WHERE status = 1
    """)
    int countEnabled();

    // 统计停用知识数量
    @Select("""
        SELECT COUNT(*)
        FROM knowledge
        WHERE status <> 1 OR status IS NULL
    """)
    int countDisabled();

    // 统计知识库涉及的分类数量
    @Select("""
        SELECT COUNT(DISTINCT category_id)
        FROM knowledge
        WHERE category_id IS NOT NULL
    """)
    int countUsedCategory();

    // 查看知识库最近更新时间
    @Select("""
        SELECT MAX(update_time)
        FROM knowledge
    """)
    String latestUpdateTime();

    @Select("""
        SELECT *
        FROM knowledge
        WHERE
            CAST(id AS CHAR) LIKE CONCAT('%', #{keyword}, '%')
            OR CAST(category_id AS CHAR) LIKE CONCAT('%', #{keyword}, '%')
            OR question LIKE CONCAT('%', #{keyword}, '%')
            OR answer LIKE CONCAT('%', #{keyword}, '%')
            OR keywords LIKE CONCAT('%', #{keyword}, '%')
            OR CAST(status AS CHAR) LIKE CONCAT('%', #{keyword}, '%')
        ORDER BY update_time DESC, id DESC
        LIMIT 500
        """)
    List<Knowledge> adminSearch(@Param("keyword") String keyword);

    // 1. 精确匹配用户问题
    @Select("""
        SELECT *
        FROM knowledge
        WHERE (status = 1 OR status IS NULL)
          AND question = #{question}
        ORDER BY view_count DESC, id DESC
        LIMIT 1
        """)
    Knowledge findExactQuestion(@Param("question") String question);


    // 2. 根据单个关键词模糊检索相似知识
    @Select("""
        SELECT *
        FROM knowledge
        WHERE (status = 1 OR status IS NULL)
          AND (
              question LIKE CONCAT('%', #{keyword}, '%')
              OR answer LIKE CONCAT('%', #{keyword}, '%')
              OR keywords LIKE CONCAT('%', #{keyword}, '%')
          )
        ORDER BY
          CASE
            WHEN question LIKE CONCAT('%', #{keyword}, '%') THEN 100
            WHEN keywords LIKE CONCAT('%', #{keyword}, '%') THEN 80
            WHEN answer LIKE CONCAT('%', #{keyword}, '%') THEN 50
            ELSE 10
          END DESC,
          view_count DESC,
          id DESC
        LIMIT 20
        """)
    List<Knowledge> searchSimilarByKeyword(@Param("keyword") String keyword);

}