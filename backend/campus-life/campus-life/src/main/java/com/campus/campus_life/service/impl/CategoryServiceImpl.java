package com.campus.campus_life.service.impl;

import com.campus.campus_life.entity.Category;
import com.campus.campus_life.mapper.CategoryMapper;
import com.campus.campus_life.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl
        implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> list() {

        return categoryMapper.list();
    }
}