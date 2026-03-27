package com.health.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.health.assessment.common.ApiResponse;
import com.health.assessment.entity.Assessment;
import com.health.assessment.entity.User;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.mapper.UserMapper;
import com.health.assessment.service.AssessmentService;
import com.health.assessment.service.DeepSeekService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 健康评测控制器
 *
 * 处理 AI 健康评测相关请求
 */
@Slf4j
@RestController
@RequestMapping("/v1/assessments")
@RequiredArgsConstructor
@Tag(name = "健康评测", description = "AI健康评测相关接口")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final DeepSeekService deepSeekService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    /**
     * 发起健康评测
     */
    @PostMapping("/evaluate")
    @Operation(summary = "发起健康评测", description = "基于已有健康数据，调用AI进行综合健康评测")
    public ApiResponse<Assessment> evaluate(HttpServletRequest request) {
        Long userId = getUserId(request);
        log.info("发起健康评测: userId={}", userId);

        Assessment assessment = assessmentService.evaluate(userId);
        return ApiResponse.success("评测完成", assessment);
    }

    /**
     * 获取最新评测报告
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新评测报告", description = "获取当前用户最新的评测报告")
    public ApiResponse<Assessment> getLatest(HttpServletRequest request) {
        Long userId = getUserId(request);
        Assessment assessment = assessmentService.getLatest(userId);
        return ApiResponse.success(assessment);
    }

    /**
     * 根据ID获取评测报告
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取评测报告", description = "根据ID获取评测报告")
    public ApiResponse<Assessment> getById(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = getUserId(request);
        Assessment assessment = assessmentService.getById(id);
        // 校验报告归属权，防止越权访问
        if (!assessment.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问该报告");
        }
        return ApiResponse.success(assessment);
    }

    /**
     * 获取评测报告列表（分页）
     */
    @GetMapping("/reports")
    @Operation(summary = "获取评测报告列表", description = "分页获取历史评测报告")
    public ApiResponse<PageInfo<Assessment>> getReports(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getUserId(request);
        PageInfo<Assessment> pageInfo = assessmentService.getReports(userId, pageNum, pageSize);
        return ApiResponse.success(pageInfo);
    }

    /**
     * 获取今年评测次数
     */
    @GetMapping("/count")
    @Operation(summary = "获取评测次数", description = "获取当年的评测次数")
    public ApiResponse<Integer> getCountCurrentYear(HttpServletRequest request) {
        Long userId = getUserId(request);
        Integer count = assessmentService.countCurrentYear(userId);
        return ApiResponse.success(count);
    }

    /**
     * 症状分析接口
     *
     * 基于用户描述的症状，通过 DeepSeek AI 进行初步分析
     * 注意：仅供参考，不构成医疗诊断
     */
    @PostMapping("/analyze-symptoms")
    @Operation(summary = "症状分析", description = "通过AI对用户描述的症状进行初步分析，仅供参考")
    public ApiResponse<Map<String, Object>> analyzeSymptoms(
            HttpServletRequest request,
            @RequestBody Map<String, String> body) {
        Long userId = getUserId(request);
        String symptoms = body.get("symptoms");

        if (symptoms == null || symptoms.trim().isEmpty()) {
            return ApiResponse.error(400, "请描述您的症状");
        }
        if (symptoms.length() > 2000) {
            return ApiResponse.error(400, "症状描述不能超过2000字");
        }

        log.info("症状分析请求: userId={}", userId);

        // 获取用户基本信息
        Map<String, Object> userInfo = new HashMap<>();
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                userInfo.put("age", user.getAge() != null ? user.getAge() : "未知");
                userInfo.put("gender", user.getGender() != null ? user.getGender() : "UNKNOWN");
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败，使用空信息进行分析: {}", e.getMessage());
        }

        // 调用 AI 进行症状分析
        String analysisResult = deepSeekService.analyzeSymptoms(symptoms.trim(), userInfo);

        try {
            Map<String, Object> resultMap = objectMapper.readValue(analysisResult,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            // 添加免责声明
            resultMap.put("disclaimer", "以上分析仅供参考，不构成医疗诊断。如有不适，请及时就医。");
            return ApiResponse.success("症状分析完成", resultMap);
        } catch (Exception e) {
            log.error("解析症状分析结果失败: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("summary", analysisResult);
            fallback.put("disclaimer", "以上分析仅供参考，不构成医疗诊断。如有不适，请及时就医。");
            return ApiResponse.success("症状分析完成", fallback);
        }
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

