package com.health.assessment.controller;

import com.github.pagehelper.PageInfo;
import com.health.assessment.common.ApiResponse;
import com.health.assessment.entity.Notification;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "系统通知", description = "用户通知相关接口")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取通知列表（分页）
     */
    @GetMapping
    @Operation(summary = "获取通知列表", description = "分页获取用户通知列表")
    public ApiResponse<PageInfo<Notification>> getNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = getUserId(request);
        PageInfo<Notification> pageInfo = notificationService.getNotifications(userId, pageNum, pageSize);
        return ApiResponse.success(pageInfo);
    }

    /**
     * 获取未读通知
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读通知", description = "获取所有未读通知")
    public ApiResponse<List<Notification>> getUnread(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Notification> list = notificationService.getUnreadNotifications(userId);
        return ApiResponse.success(list);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读通知数量", description = "获取未读通知总数")
    public ApiResponse<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserId(request);
        Integer count = notificationService.countUnread(userId);
        return ApiResponse.success(count);
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "标记指定通知为已读")
    public ApiResponse<String> markAsRead(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        notificationService.markAsRead(userId, id);
        return ApiResponse.success("已标记为已读", "");
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读", description = "将所有通知标记为已读")
    public ApiResponse<String> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserId(request);
        notificationService.markAllAsRead(userId);
        return ApiResponse.success("全部已标记为已读", "");
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除指定通知")
    public ApiResponse<String> delete(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        notificationService.delete(userId, id);
        return ApiResponse.success("通知已删除", "");
    }

    // ============ 私有方法 ============

    private Long getUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new AuthenticationException("未能获取用户ID，请重新登录");
        }
        return userId;
    }
}

