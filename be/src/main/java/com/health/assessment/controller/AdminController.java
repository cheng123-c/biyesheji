package com.health.assessment.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.health.assessment.common.ApiResponse;
import com.health.assessment.entity.User;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.mapper.AssessmentMapper;
import com.health.assessment.mapper.HealthDataMapper;
import com.health.assessment.mapper.InterventionPlanMapper;
import com.health.assessment.mapper.MedicalRecordMapper;
import com.health.assessment.mapper.NotificationMapper;
import com.health.assessment.mapper.UserMapper;
import com.health.assessment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 管理员后台控制器
 *
 * 提供用户管理、数据统计、系统监控等管理功能
 * 注意：生产环境应增加管理员权限校验（RBAC），当前版本为简化实现
 */
@Slf4j
@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@Tag(name = "管理员后台", description = "系统管理与数据统计接口")
public class AdminController {

    private final UserMapper userMapper;
    private final UserService userService;
    private final AssessmentMapper assessmentMapper;
    private final HealthDataMapper healthDataMapper;
    private final NotificationMapper notificationMapper;
    private final InterventionPlanMapper interventionPlanMapper;
    private final MedicalRecordMapper medicalRecordMapper;

    /**
     * 获取系统统计总览
     */
    @GetMapping("/statistics/overview")
    @Operation(summary = "系统统计总览", description = "获取系统关键指标汇总（用户数、评测数等）")
    public ApiResponse<Map<String, Object>> getStatisticsOverview(HttpServletRequest request) {
        getUserId(request); // 验证登录状态
        log.info("获取系统统计总览");

        Map<String, Object> stats = new HashMap<>();

        // 用户统计
        try {
            List<User> activeUsers = userMapper.selectAllActiveUsers();
            stats.put("totalActiveUsers", activeUsers.size());
        } catch (Exception e) {
            stats.put("totalActiveUsers", 0);
        }

        // 今日评测数
        try {
            List<Map<String, Object>> riskCounts = assessmentMapper.countByRiskLevel(LocalDate.now());
            int todayTotal = riskCounts.stream()
                    .mapToInt(m -> ((Number) m.get("count")).intValue())
                    .sum();
            stats.put("todayAssessments", todayTotal);
            stats.put("riskDistribution", riskCounts);
        } catch (Exception e) {
            stats.put("todayAssessments", 0);
            stats.put("riskDistribution", List.of());
        }

        // 高风险报告数（最近7天）
        try {
            List<?> highRiskReports = assessmentMapper.selectHighRiskReports(LocalDate.now().minusDays(7));
            stats.put("recentHighRiskCount", highRiskReports.size());
        } catch (Exception e) {
            stats.put("recentHighRiskCount", 0);
        }

        stats.put("systemStatus", "RUNNING");
        stats.put("timestamp", System.currentTimeMillis());

        return ApiResponse.success(stats);
    }

    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/users")
    @Operation(summary = "获取用户列表", description = "分页获取系统中所有活跃用户")
    public ApiResponse<PageInfo<User>> getUserList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        getUserId(request); // 验证登录状态
        log.info("管理员查询用户列表: page={}, size={}", pageNum, pageSize);

        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userMapper.selectAllActiveUsers();
        // 隐藏密码哈希
        users.forEach(u -> u.setPasswordHash(null));
        PageInfo<User> pageInfo = new PageInfo<>(users);
        return ApiResponse.success(pageInfo);
    }

    /**
     * 获取指定用户详情
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    public ApiResponse<User> getUserDetail(
            HttpServletRequest request,
            @PathVariable Long userId) {
        getUserId(request); // 验证登录状态
        log.info("管理员查询用户详情: userId={}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        user.setPasswordHash(null); // 隐藏密码
        return ApiResponse.success(user);
    }

    /**
     * 更新用户状态（禁用/启用）
     */
    @PutMapping("/users/{userId}/status")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户账号")
    public ApiResponse<Void> updateUserStatus(
            HttpServletRequest request,
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        getUserId(request); // 验证登录状态
        String status = body.get("status");
        if (status == null || !status.matches("ACTIVE|INACTIVE|BANNED")) {
            return ApiResponse.error(400, "无效的状态值，允许值为: ACTIVE, INACTIVE, BANNED");
        }

        log.info("管理员更新用户状态: userId={}, status={}", userId, status);
        userService.updateUserStatus(userId, status);
        return ApiResponse.success(null);
    }

    /**
     * 删除用户（软删除）
     */
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "删除用户", description = "软删除指定用户")
    public ApiResponse<Void> deleteUser(
            HttpServletRequest request,
            @PathVariable Long userId) {
        Long adminUserId = getUserId(request);

        // 防止管理员删除自己
        if (adminUserId.equals(userId)) {
            return ApiResponse.error(400, "不能删除自己的账号");
        }

        log.info("管理员删除用户: userId={}", userId);
        userService.deleteUser(userId);
        return ApiResponse.success(null);
    }

    /**
     * 获取高风险评测报告列表（最近30天）
     */
    @GetMapping("/assessments/high-risk")
    @Operation(summary = "高风险评测报告", description = "获取最近30天内的高风险评测报告")
    public ApiResponse<List<?>> getHighRiskAssessments(
            HttpServletRequest request,
            @RequestParam(defaultValue = "30") int days) {
        getUserId(request);
        log.info("获取高风险评测报告: days={}", days);

        List<?> reports = assessmentMapper.selectHighRiskReports(LocalDate.now().minusDays(days));
        return ApiResponse.success(reports);
    }

    /**
     * 获取评测风险等级分布统计
     */
    @GetMapping("/statistics/risk-distribution")
    @Operation(summary = "风险分布统计", description = "获取各风险等级的评测报告数量分布")
    public ApiResponse<List<Map<String, Object>>> getRiskDistribution(
            HttpServletRequest request,
            @RequestParam(defaultValue = "30") int days) {
        getUserId(request);
        log.info("获取风险分布统计: days={}", days);

        List<Map<String, Object>> distribution = assessmentMapper.countByRiskLevel(
                LocalDate.now().minusDays(days));
        return ApiResponse.success(distribution);
    }

    /**
     * 获取系统健康状态（用于运维监控）
     */
    @GetMapping("/system/health")
    @Operation(summary = "系统健康检查", description = "检查各模块运行状态")
    public ApiResponse<Map<String, Object>> getSystemHealth(HttpServletRequest request) {
        getUserId(request);

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("database", "UP");
        health.put("aiService", "UNKNOWN"); // 需要实际检查
        health.put("timestamp", System.currentTimeMillis());

        // 简单数据库连通性检查
        try {
            userMapper.selectAllActiveUsers();
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("status", "DEGRADED");
        }

        return ApiResponse.success(health);
    }

    // ============ 私有方法 ============

    /**
     * 获取用户ID并验证管理员权限
     * 只有 ADMIN 角色的用户才能访问管理员接口
     */
    private Long getUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new AuthenticationException("未能获取用户ID，请重新登录");
        }

        // 验证管理员权限
        User user = userMapper.selectById(userId);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new AuthenticationException(403, "权限不足，仅管理员可访问此接口");
        }

        return userId;
    }
}

