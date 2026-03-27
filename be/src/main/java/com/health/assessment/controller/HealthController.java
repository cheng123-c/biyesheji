package com.health.assessment.controller;

import com.health.assessment.common.ApiResponse;
import com.health.assessment.entity.Assessment;
import com.health.assessment.entity.HealthData;
import com.health.assessment.entity.HealthSuggestion;
import com.health.assessment.entity.Notification;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.mapper.AssessmentMapper;
import com.health.assessment.mapper.HealthDataMapper;
import com.health.assessment.mapper.HealthSuggestionMapper;
import com.health.assessment.mapper.InterventionPlanMapper;
import com.health.assessment.mapper.MedicalRecordMapper;
import com.health.assessment.mapper.NotificationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 健康概览控制器
 *
 * 提供用户健康数据总览、系统状态检查等功能
 */
@Slf4j
@RestController
@RequestMapping("/v1/health")
@RequiredArgsConstructor
@Tag(name = "健康概览", description = "健康数据概览与系统状态接口")
public class HealthController {

    private final HealthDataMapper healthDataMapper;
    private final AssessmentMapper assessmentMapper;
    private final NotificationMapper notificationMapper;
    private final HealthSuggestionMapper healthSuggestionMapper;
    private final InterventionPlanMapper interventionPlanMapper;
    private final MedicalRecordMapper medicalRecordMapper;

    /**
     * 系统健康检查（公开接口）
     */
    @GetMapping("/status")
    @Operation(summary = "系统状态检查", description = "检查后端服务运行状态，无需认证")
    public ApiResponse<Map<String, Object>> status() {
        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("status", "UP");
        statusInfo.put("service", "健康评测系统");
        statusInfo.put("version", "1.0.0");
        statusInfo.put("timestamp", System.currentTimeMillis());
        return ApiResponse.success(statusInfo);
    }

    /**
     * 用户健康数据总览
     *
     * 汇总用户的关键健康指标，包括：
     * - 最新健康评分
     * - 最新健康数据
     * - 未读通知数
     * - 未读建议数
     * - 进行中的干预方案数
     * - 医疗记录总数
     */
    @GetMapping("/overview")
    @Operation(summary = "健康数据总览", description = "获取当前用户的健康数据汇总信息")
    public ApiResponse<Map<String, Object>> overview(HttpServletRequest request) {
        Long userId = getUserId(request);
        log.info("获取健康总览: userId={}", userId);

        Map<String, Object> overview = new HashMap<>();

        // 最新评测报告
        try {
            Assessment latestAssessment = assessmentMapper.selectLatestByUserId(userId);
            if (latestAssessment != null) {
                Map<String, Object> assessmentInfo = new HashMap<>();
                assessmentInfo.put("id", latestAssessment.getId());
                assessmentInfo.put("overallScore", latestAssessment.getOverallScore());
                assessmentInfo.put("riskLevel", latestAssessment.getRiskLevel());
                assessmentInfo.put("assessmentDate", latestAssessment.getAssessmentDate());
                assessmentInfo.put("summary", latestAssessment.getSummary());
                overview.put("latestAssessment", assessmentInfo);
            } else {
                overview.put("latestAssessment", null);
            }
        } catch (Exception e) {
            log.warn("获取评测信息失败: {}", e.getMessage());
            overview.put("latestAssessment", null);
        }

        // 最新健康数据（各类型最新值）
        try {
            List<HealthData> latestData = healthDataMapper.selectLatestAllTypes(userId);
            overview.put("latestHealthData", latestData);
            overview.put("dataTypeCount", latestData.size());
        } catch (Exception e) {
            log.warn("获取健康数据失败: {}", e.getMessage());
            overview.put("latestHealthData", List.of());
            overview.put("dataTypeCount", 0);
        }

        // 未读通知数
        try {
            Integer unreadNotifications = notificationMapper.countUnreadByUserId(userId);
            overview.put("unreadNotifications", unreadNotifications != null ? unreadNotifications : 0);
        } catch (Exception e) {
            overview.put("unreadNotifications", 0);
        }

        // 未读建议数
        try {
            Integer unreadSuggestions = healthSuggestionMapper.countUnreadByUserId(userId);
            overview.put("unreadSuggestions", unreadSuggestions != null ? unreadSuggestions : 0);
        } catch (Exception e) {
            overview.put("unreadSuggestions", 0);
        }

        // 进行中的干预方案数
        try {
            Integer activePlans = interventionPlanMapper.countActiveByUserId(userId);
            overview.put("activePlans", activePlans != null ? activePlans : 0);
        } catch (Exception e) {
            overview.put("activePlans", 0);
        }

        // 医疗记录总数
        try {
            Integer medicalRecordCount = medicalRecordMapper.countByUserId(userId);
            overview.put("medicalRecordCount", medicalRecordCount != null ? medicalRecordCount : 0);
        } catch (Exception e) {
            overview.put("medicalRecordCount", 0);
        }

        // 今年评测次数
        try {
            Integer assessmentCountThisYear = assessmentMapper.countCurrentYearByUserId(userId);
            overview.put("assessmentCountThisYear", assessmentCountThisYear != null ? assessmentCountThisYear : 0);
        } catch (Exception e) {
            overview.put("assessmentCountThisYear", 0);
        }

        return ApiResponse.success(overview);
    }

    /**
     * 获取用户最近的健康建议（前5条高优先级）
     */
    @GetMapping("/quick-suggestions")
    @Operation(summary = "快速健康建议", description = "获取用户最近的高优先级健康建议")
    public ApiResponse<List<HealthSuggestion>> getQuickSuggestions(HttpServletRequest request) {
        Long userId = getUserId(request);
        log.debug("获取快速健康建议: userId={}", userId);

        List<HealthSuggestion> suggestions = healthSuggestionMapper.selectByUserIdAndPriority(userId, "HIGH");
        if (suggestions.size() > 5) {
            suggestions = suggestions.subList(0, 5);
        }
        return ApiResponse.success(suggestions);
    }

    /**
     * 获取用户最近的未读通知（前10条）
     */
    @GetMapping("/recent-notifications")
    @Operation(summary = "最近通知", description = "获取用户最近的未读通知")
    public ApiResponse<List<Notification>> getRecentNotifications(HttpServletRequest request) {
        Long userId = getUserId(request);
        log.debug("获取最近通知: userId={}", userId);

        List<Notification> notifications = notificationMapper.selectUnreadByUserId(userId);
        if (notifications.size() > 10) {
            notifications = notifications.subList(0, 10);
        }
        return ApiResponse.success(notifications);
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

