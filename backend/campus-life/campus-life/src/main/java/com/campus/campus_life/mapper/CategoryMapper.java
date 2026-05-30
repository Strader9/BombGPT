package com.campus.campus_life.mapper;

import com.campus.campus_life.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("""
        select *
        from category
        order by sort_order
    """)
    List<Category> list();
}