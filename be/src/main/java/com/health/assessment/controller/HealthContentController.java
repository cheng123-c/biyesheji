package com.health.assessment.controller;

import com.github.pagehelper.PageInfo;
import com.health.assessment.common.ApiResponse;
import com.health.assessment.entity.HealthContent;
import com.health.assessment.service.HealthContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 健康内容库控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/health-content")
@RequiredArgsConstructor
@Tag(name = "健康内容库", description = "健康知识文章相关接口")
public class HealthContentController {

    private final HealthContentService healthContentService;

    // ==================== 用户端（公开接口） ====================

    /**
     * 获取已发布内容列表（分页）
     */
    @GetMapping
    @Operation(summary = "获取健康内容列表", description = "获取已发布的健康知识文章")
    public ApiResponse<PageInfo<HealthContent>> getPublished(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String contentType) {
        return ApiResponse.success(healthContentService.getPublished(pageNum, pageSize, contentType));
    }

    /**
     * 搜索健康内容
     */
    @GetMapping("/search")
    @Operation(summary = "搜索健康内容", description = "根据关键词搜索健康知识文章")
    public ApiResponse<List<HealthContent>> search(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(healthContentService.search(keyword));
    }

    /**
     * 获取内容详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取内容详情", description = "获取指定健康内容的详细信息")
    public ApiResponse<HealthContent> getById(@PathVariable Long id) {
        return ApiResponse.success(healthContentService.getById(id));
    }

    // ==================== 管理员端 ====================

    /**
     * 管理员获取全部内容（含未发布）
     */
    @GetMapping("/admin/all")
    @Operation(summary = "获取全部内容（管理员）", description = "管理员获取所有内容，含未发布")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageInfo<HealthContent>> adminGetAll(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(healthContentService.getAll(pageNum, pageSize));
    }

    /**
     * 管理员创建内容
     */
    @PostMapping("/admin")
    @Operation(summary = "创建内容（管理员）", description = "管理员创建健康知识内容")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HealthContent> adminCreate(@RequestBody HealthContent content) {
        log.info("管理员创建内容: title={}", content.getContentTitle());
        return ApiResponse.success("创建成功", healthContentService.create(content));
    }

    /**
     * 管理员更新内容
     */
    @PutMapping("/admin/{id}")
    @Operation(summary = "更新内容（管理员）", description = "管理员更新健康知识内容")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HealthContent> adminUpdate(
            @PathVariable Long id,
            @RequestBody HealthContent content) {
        return ApiResponse.success("更新成功", healthContentService.update(id, content));
    }

    /**
     * 管理员删除内容
     */
    @DeleteMapping("/admin/{id}")
    @Operation(summary = "删除内容（管理员）", description = "管理员删除健康知识内容")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> adminDelete(@PathVariable Long id) {
        healthContentService.delete(id);
        return ApiResponse.success("删除成功", "");
    }

    /**
     * 管理员发布/取消发布内容
     */
    @PatchMapping("/admin/{id}/publish")
    @Operation(summary = "切换发布状态（管理员）", description = "管理员发布或取消发布内容")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HealthContent> adminTogglePublish(@PathVariable Long id) {
        return ApiResponse.success("操作成功", healthContentService.togglePublish(id));
    }
}

