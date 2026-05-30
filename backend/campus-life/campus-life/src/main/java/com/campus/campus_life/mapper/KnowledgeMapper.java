package com.campus.campus_life.mapper;

import com.campus.campus_life.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface KnowledgeMapper {

    // 1. 查询全部（对应 Service list()）
    @Select("SELECT * FROM knowledge")
    List<Knowledge> list();

    // 2. 按分类查询（和你前面说的 selectByCategoryId 对齐）
    @Select("SELECT * FROM knowledge WHERE category_id = #{categoryId}")
    List<Knowledge> selectByCategoryId(@Param("categoryId") Long categoryId);

    // 3. 搜索
    @Select("SELECT * FROM knowledge WHERE title LIKE CONCAT('%', #{keyword}, '%')")
    List<Knowledge> search(@Param("keyword") String keyword);

    // 4. 根据ID查询详情
    @Select("SELECT * FROM knowledge WHERE id = #{id}")
    Knowledge selectById(@Param("id") Long id);

    // 5. 热门TOP10
    @Select("select * from knowledge order by view_count desc limit 10")
    List<Knowledge> listHot();

    // 6. 阅读数+1
    @Update("update knowledge set view_count = view_count + 1 where id = #{id}")
    void increaseViewCount(@Param("id") Long id);
}