package com.health.assessment.controller;

import com.github.pagehelper.PageInfo;
import com.health.assessment.common.ApiResponse;
import com.health.assessment.dto.HealthSuggestionDTO;
import com.health.assessment.entity.HealthSuggestion;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.service.HealthSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 健康建议控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/suggestions")
@RequiredArgsConstructor
@Tag(name = "健康建议管理", description = "健康建议相关接口")
public class SuggestionController {

    private final HealthSuggestionService healthSuggestionService;

    /**
     * 获取健康建议列表（分页）
     */
    @GetMapping
    @Operation(summary = "获取健康建议列表", description = "分页获取用户的健康建议")
    public ApiResponse<PageInfo<HealthSuggestion>> getList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String priority) {
        Long userId = getUserId(request);
        PageInfo<HealthSuggestion> pageInfo = healthSuggestionService.getSuggestions(userId, pageNum, pageSize, priority);
        return ApiResponse.success(pageInfo);
    }

    /**
     * 获取未读健康建议
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读建议", description = "获取用户未读的健康建议")
    public ApiResponse<List<HealthSuggestion>> getUnread(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<HealthSuggestion> list = healthSuggestionService.getUnreadSuggestions(userId);
        return ApiResponse.success(list);
    }

    /**
     * 获取未读建议数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读建议数量", description = "获取用户未读建议的数量")
    public ApiResponse<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserId(request);
        Integer count = healthSuggestionService.countUnread(userId);
        return ApiResponse.success(count);
    }

    /**
     * 根据ID获取健康建议
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取建议详情", description = "根据ID获取健康建议详情")
    public ApiResponse<HealthSuggestion> getById(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        HealthSuggestion suggestion = healthSuggestionService.getById(id, userId);
        return ApiResponse.success(suggestion);
    }

    /**
     * 创建健康建议
     */
    @PostMapping
    @Operation(summary = "创建健康建议", description = "手动创建健康建议")
    public ApiResponse<HealthSuggestion> create(
            HttpServletRequest request,
            @Valid @RequestBody HealthSuggestionDTO dto) {
        Long userId = getUserId(request);
        log.info("创建健康建议: userId={}, type={}", userId, dto.getSuggestionType());

        HealthSuggestion suggestion = buildSuggestion(dto, userId);
        HealthSuggestion saved = healthSuggestionService.create(userId, suggestion);
        return ApiResponse.success("创建成功", saved);
    }

    /**
     * 标记建议为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "将指定建议标记为已读")
    public ApiResponse<String> markAsRead(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        healthSuggestionService.markAsRead(userId, id);
        return ApiResponse.success("已标记为已读", "");
    }

    /**
     * 标记所有建议为已读
     */
    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读", description = "将所有建议标记为已读")
    public ApiResponse<String> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserId(request);
        healthSuggestionService.markAllAsRead(userId);
        return ApiResponse.success("已全部标记为已读", "");
    }

    /**
     * 删除健康建议
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除建议", description = "删除指定的健康建议")
    public ApiResponse<String> delete(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        healthSuggestionService.delete(userId, id);
        return ApiResponse.success("删除成功", "");
    }

    // ============ 私有方法 ============

    private Long getUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new AuthenticationException("未能获取用户ID，请重新登录");
        }
        return userId;
    }

    private HealthSuggestion buildSuggestion(HealthSuggestionDTO dto, Long userId) {
        return HealthSuggestion.builder()
                .userId(userId)
                .suggestionType(dto.getSuggestionType())
                .suggestionContent(dto.getSuggestionContent())
                .priority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM")
                .evidenceLevel(dto.getEvidenceLevel())
                .createdBy(userId)
                .build();
    }
}

