package com.health.assessment.controller;

import com.github.pagehelper.PageInfo;
import com.health.assessment.common.ApiResponse;
import com.health.assessment.entity.UserFeedback;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.service.UserFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户反馈控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/feedback")
@RequiredArgsConstructor
@Tag(name = "用户反馈", description = "用户反馈相关接口")
public class UserFeedbackController {

    private final UserFeedbackService userFeedbackService;

    // ==================== 用户端接口 ====================

    /**
     * 提交反馈
     */
    @PostMapping
    @Operation(summary = "提交反馈", description = "用户提交意见反馈")
    public ApiResponse<UserFeedback> submit(
            HttpServletRequest request,
            @Valid @RequestBody FeedbackSubmitRequest body) {
        Long userId = getUserId(request);
        UserFeedback feedback = UserFeedback.builder()
                .feedbackType(body.getFeedbackType())
                .feedbackTitle(body.getFeedbackTitle())
                .feedbackContent(body.getFeedbackContent())
                .contactInfo(body.getContactInfo())
                .build();
        return ApiResponse.success("提交成功", userFeedbackService.submit(userId, feedback));
    }

    /**
     * 获取我的反馈列表
     */
    @GetMapping("/my")
    @Operation(summary = "我的反馈", description = "获取当前用户的反馈列表")
    public ApiResponse<List<UserFeedback>> getMy(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ApiResponse.success(userFeedbackService.getMyFeedbacks(userId));
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/my/{id}")
    @Operation(summary = "反馈详情", description = "获取指定反馈的详情")
    public ApiResponse<UserFeedback> getById(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        return ApiResponse.success(userFeedbackService.getById(id, userId));
    }

    // ==================== 管理员端接口 ====================

    /**
     * 管理员获取全部反馈
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "所有反馈（管理员）", description = "管理员分页查询所有反馈")
    public ApiResponse<PageInfo<UserFeedback>> adminGetAll(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "15") int pageSize,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(userFeedbackService.adminGetAll(pageNum, pageSize, status));
    }

    /**
     * 管理员回复反馈
     */
    @PostMapping("/admin/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "回复反馈（管理员）", description = "管理员回复用户反馈")
    public ApiResponse<UserFeedback> adminReply(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reply = body.getOrDefault("reply", "");
        String status = body.getOrDefault("status", "RESOLVED");
        return ApiResponse.success("回复成功", userFeedbackService.adminReply(id, reply, status));
    }

    /**
     * 管理员更改状态
     */
    @PatchMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更改状态（管理员）", description = "管理员更改反馈处理状态")
    public ApiResponse<String> adminUpdateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "PROCESSING");
        userFeedbackService.adminUpdateStatus(id, status);
        return ApiResponse.success("更新成功", "");
    }

    /**
     * 待处理反馈数量
     */
    @GetMapping("/admin/pending-count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "待处理数量（管理员）", description = "统计待处理的反馈数量")
    public ApiResponse<Integer> adminCountPending() {
        return ApiResponse.success(userFeedbackService.countPending());
    }

    // ==================== 私有方法 ====================

    private Long getUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw new AuthenticationException("未能获取用户ID，请重新登录");
        return userId;
    }

    @Data
    static class FeedbackSubmitRequest {
        @NotBlank(message = "反馈类型不能为空")
        private String feedbackType;

        @NotBlank(message = "反馈标题不能为空")
        @Size(max = 200, message = "标题不超过200字符")
        private String feedbackTitle;

        @NotBlank(message = "反馈内容不能为空")
        @Size(max = 2000, message = "内容不超过2000字符")
        private String feedbackContent;

        @Size(max = 100, message = "联系方式不超过100字符")
        private String contactInfo;
    }
}

