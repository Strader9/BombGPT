package com.campus.campus_life.controller;

import com.campus.campus_life.entity.Knowledge;
import com.campus.campus_life.service.KnowledgeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class KnowledgeController {

    @Resource
    private KnowledgeService knowledgeService;

    // 1. 查询全部知识库（和 Service.list() 对应）
    @GetMapping("/knowledge/list")
    public List<Knowledge> list() {
        return knowledgeService.list();
    }

    // 2. 按分类查询（和 Service.listByCategory() 对应）
    @GetMapping("/knowledge/category")
    public List<Knowledge> listByCategory(
            @RequestParam Long categoryId) {
        return knowledgeService.listByCategory(categoryId);
    }

    // 3. 搜索知识库
    @GetMapping("/knowledge/search")
    public List<Knowledge> search(
            @RequestParam(required = false) String keyword) {
        return knowledgeService.search(keyword);
    }

    // 4. 热门TOP10
    @GetMapping("/knowledge/hot")
    public List<Knowledge> listHot() {
        return knowledgeService.listHot();
    }

    // 5. 根据ID查询详情
    @GetMapping("/knowledge/detail")
    public Knowledge getById(@RequestParam Long id) {
        return knowledgeService.getById(id);
    }
}