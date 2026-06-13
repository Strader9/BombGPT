package com.campus.campus_life.service;

import com.campus.campus_life.entity.Knowledge;
import java.util.List;

import java.util.List;

public interface KnowledgeService {

    List<Knowledge> list();

    List<Knowledge> listByCategory(Long categoryId);

    List<Knowledge> search(String keyword);

    Knowledge getById(Long id);

    List<Knowledge> listHot();

    List<Knowledge> listPage(Integer pageNum, Integer pageSize);

    List<Knowledge> adminSearch(String keyword);

    int countAll();

    void increaseViewCount(Long id);

    void add(Knowledge knowledge);

    void update(Knowledge knowledge);

    void delete(Long id);
}