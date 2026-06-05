package com.campus.campus_life.service.impl;

import com.campus.campus_life.entity.Knowledge;
import com.campus.campus_life.mapper.KnowledgeMapper;
import com.campus.campus_life.service.KnowledgeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    @Resource
    private KnowledgeMapper knowledgeMapper;

    @Override
    public List<Knowledge> list() {
        return knowledgeMapper.list();
    }

    @Override
    public List<Knowledge> listByCategory(Long categoryId) {
        return knowledgeMapper.selectByCategoryId(categoryId);
    }

    @Override
    public List<Knowledge> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return knowledgeMapper.list();
        }

        return knowledgeMapper.search(keyword);
    }

    @Override
    public Knowledge getById(Long id) {
        knowledgeMapper.increaseViewCount(id);
        return knowledgeMapper.selectById(id);
    }

    @Override
    public List<Knowledge> listHot() {
        return knowledgeMapper.listHot();
    }

    @Override
    public void increaseViewCount(Long id) {
        knowledgeMapper.increaseViewCount(id);
    }

    @Override
    public void add(Knowledge knowledge) {
        knowledgeMapper.insert(knowledge);
    }

    @Override
    public void update(Knowledge knowledge) {
        knowledgeMapper.update(knowledge);
    }

    @Override
    public void delete(Long id) {
        knowledgeMapper.deleteById(id);
    }
}