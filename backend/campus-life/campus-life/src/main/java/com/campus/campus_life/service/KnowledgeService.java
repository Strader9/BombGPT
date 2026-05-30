package com.campus.campus_life.service;

import com.campus.campus_life.entity.Knowledge;
import java.util.List;

public interface KnowledgeService {

    // 1. 查询全部知识库
    List<Knowledge> list();

    // 2. 根据分类ID查询
    List<Knowledge> listByCategory(Long categoryId);

    // 3. 搜索知识库
    List<Knowledge> search(String keyword);

    // 4. 根据ID查询详情
    Knowledge getById(Long id);

    // 5. 热门TOP10
    List<Knowledge> listHot();

    // 6. 阅读数+1
    void increaseViewCount(Long id);
}