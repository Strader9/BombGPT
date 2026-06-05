package com.campus.campus_life.controller;

import com.campus.campus_life.entity.Knowledge;
import com.campus.campus_life.service.KnowledgeService;
import com.campus.campus_life.utils.AuthUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class KnowledgeController {

    @Resource
    private KnowledgeService knowledgeService;

    // ====================== 客户端接口 ======================

    @GetMapping("/knowledge/list")
    public List<Knowledge> list() {
        return knowledgeService.list();
    }

    @GetMapping("/knowledge/category")
    public List<Knowledge> listByCategory(@RequestParam Long categoryId) {
        return knowledgeService.listByCategory(categoryId);
    }

    @GetMapping("/knowledge/search")
    public List<Knowledge> search(@RequestParam(required = false) String keyword) {
        return knowledgeService.search(keyword);
    }

    @GetMapping("/knowledge/hot")
    public List<Knowledge> listHot() {
        return knowledgeService.listHot();
    }

    @GetMapping("/knowledge/detail")
    public Knowledge getById(@RequestParam Long id) {
        return knowledgeService.getById(id);
    }

    // ====================== 管理端接口 ======================

    @GetMapping("/admin/knowledge/list")
    public Map<String, Object> adminList(HttpServletRequest request) {

        if (!AuthUtil.isAdmin(request)) {
            return Map.of(
                    "code", "403",
                    "msg", "无管理员权限"
            );
        }

        return Map.of(
                "code", "200",
                "msg", "查询成功",
                "data", knowledgeService.list()
        );
    }

    @PostMapping("/admin/knowledge/add")
    public Map<String, Object> add(
            @RequestBody Knowledge knowledge,
            HttpServletRequest request
    ) {

        if (!AuthUtil.isAdmin(request)) {
            return Map.of(
                    "code", "403",
                    "msg", "无管理员权限"
            );
        }

        if (knowledge.getQuestion() == null || knowledge.getQuestion().trim().isEmpty()) {
            return Map.of(
                    "code", "400",
                    "msg", "问题不能为空"
            );
        }

        if (knowledge.getAnswer() == null || knowledge.getAnswer().trim().isEmpty()) {
            return Map.of(
                    "code", "400",
                    "msg", "答案不能为空"
            );
        }

        if (knowledge.getStatus() == null) {
            knowledge.setStatus(1);
        }

        if (knowledge.getCategoryId() == null) {
            knowledge.setCategoryId(1L);
        }

        String username = (String) request.getAttribute("username");
        knowledge.setContributor(username);

        knowledgeService.add(knowledge);

        return Map.of(
                "code", "200",
                "msg", "新增成功"
        );
    }

    @PostMapping("/admin/knowledge/update")
    public Map<String, Object> update(
            @RequestBody Knowledge knowledge,
            HttpServletRequest request
    ) {

        if (!AuthUtil.isAdmin(request)) {
            return Map.of(
                    "code", "403",
                    "msg", "无管理员权限"
            );
        }

        if (knowledge.getId() == null) {
            return Map.of(
                    "code", "400",
                    "msg", "ID不能为空"
            );
        }

        if (knowledge.getStatus() == null) {
            knowledge.setStatus(1);
        }

        if (knowledge.getCategoryId() == null) {
            knowledge.setCategoryId(1L);
        }

        knowledgeService.update(knowledge);

        return Map.of(
                "code", "200",
                "msg", "修改成功"
        );
    }

    @PostMapping("/admin/knowledge/delete")
    public Map<String, Object> delete(
            @RequestBody Map<String, Object> map,
            HttpServletRequest request
    ) {

        if (!AuthUtil.isAdmin(request)) {
            return Map.of(
                    "code", "403",
                    "msg", "无管理员权限"
            );
        }

        Long id = Long.valueOf(map.get("id").toString());

        knowledgeService.delete(id);

        return Map.of(
                "code", "200",
                "msg", "删除成功"
        );
    }
}