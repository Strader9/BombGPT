package com.campus.campus_life.service;

import com.campus.campus_life.entity.Knowledge;

import java.util.List;

public interface KnowledgeService {

    List<Knowledge> list();

    List<Knowledge> listByCategory(Long categoryId);

    List<Knowledge> search(String keyword);

    Knowledge getById(Long id);

    List<Knowledge> listHot();

    void increaseViewCount(Long id);

    void add(Knowledge knowledge);

    void update(Knowledge knowledge);

    void delete(Long id);
}