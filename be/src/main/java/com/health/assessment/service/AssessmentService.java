package com.health.assessment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.health.assessment.entity.Assessment;
import com.health.assessment.entity.HealthData;
import com.health.assessment.entity.Notification;
import com.health.assessment.entity.User;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.exception.ResourceNotFoundException;
import com.health.assessment.mapper.AssessmentMapper;
import com.health.assessment.mapper.HealthDataMapper;
import com.health.assessment.mapper.NotificationMapper;
import com.health.assessment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评测服务
 *
 * 功能：
 * - 发起 AI 评测
 * - 数据特征提取
 * - 报告生成与存储
 * - 评测历史查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentMapper assessmentMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final HealthDataMapper healthDataMapper;
    private final HealthDataService healthDataService;
    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper;

    /**
     * 发起健康评测
     *
     * @param userId 用户ID
     * @return 评测报告
     */
    @Transactional(rollbackFor = Exception.class)
    public Assessment evaluate(Long userId) {
        log.info("发起健康评测: userId={}", userId);

        // 检查今天是否已评测
        LocalDate today = LocalDate.now();
        Assessment existing = assessmentMapper.selectByUserIdAndDate(userId, today);
        if (existing != null) {
            log.info("今天已有评测记录，返回最新结果: userId={}", userId);
            return existing;
        }

        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        // 获取最近30天的健康数据（直接查询所有类型，不使用分页，避免PageHelper线程污染）
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();
        List<HealthData> recentData = healthDataMapper.selectByUserIdAndTimeRange(userId, startTime, endTime);

        if (recentData.isEmpty()) {
            throw new BusinessException("暂无健康数据，请先上传健康数据再进行评测");
        }

        // 提取特征数据
        String healthDataSummary = extractFeatures(recentData);

        // 构建用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("age", user.getAge() != null ? user.getAge() : "未知");
        userInfo.put("gender", user.getGender() != null ? user.getGender() : "UNKNOWN");
        userInfo.put("realName", user.getRealName() != null ? user.getRealName() : "用户");

        // 调用 DeepSeek AI
        String aiResult = deepSeekService.analyzeHealthData(healthDataSummary, userInfo);

        // 解析 AI 结果并生成报告
        Assessment assessment = buildAssessment(userId, aiResult);

        // 保存报告
        assessmentMapper.insert(assessment);
        log.info("评测报告生成成功: userId={}, id={}", userId, assessment.getId());

        // 推送通知
        sendAssessmentNotification(userId, assessment);

        return assessment;
    }

    /**
     * 根据ID获取评测报告
     */
    public Assessment getById(Long id) {
        Assessment assessment = assessmentMapper.selectById(id);
        if (assessment == null) {
            throw new ResourceNotFoundException("评测报告不存在: " + id);
        }
        return assessment;
    }

    /**
     * 获取用户最新评测报告
     */
    public Assessment getLatest(Long userId) {
        Assessment assessment = assessmentMapper.selectLatestByUserId(userId);
        if (assessment == null) {
            throw new ResourceNotFoundException("暂无评测报告，请先发起评测");
        }
        return assessment;
    }

    /**
     * 获取用户评测报告列表（分页）
     */
    public PageInfo<Assessment> getReports(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Assessment> list = assessmentMapper.selectByUserId(userId);
        return new PageInfo<>(list);
    }

    /**
     * 获取用户今年评测次数
     */
    public Integer countCurrentYear(Long userId) {
        return assessmentMapper.countCurrentYearByUserId(userId);
    }

    /**
     * 特征提取 - 将健康数据转换为 AI 可分析的摘要格式
     */
    private String extractFeatures(List<HealthData> dataList) {
        Map<String, Object> features = new HashMap<>();

        // 按类型分组，计算各类型的最新值和统计数据
        Map<String, List<BigDecimal>> grouped = new HashMap<>();
        for (HealthData hd : dataList) {
            grouped.computeIfAbsent(hd.getDataType(), k -> new java.util.ArrayList<>())
                    .add(hd.getDataValue());
        }

        for (Map.Entry<String, List<BigDecimal>> entry : grouped.entrySet()) {
            String type = entry.getKey();
            List<BigDecimal> values = entry.getValue();
            if (!values.isEmpty()) {
                BigDecimal latest = values.get(0);
                double avg = values.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0);
                double max = values.stream().mapToDouble(BigDecimal::doubleValue).max().orElse(0);
                double min = values.stream().mapToDouble(BigDecimal::doubleValue).min().orElse(0);

                Map<String, Object> typeStat = new HashMap<>();
                typeStat.put("latest", latest);
                typeStat.put("avg", Math.round(avg * 100.0) / 100.0);
                typeStat.put("max", max);
                typeStat.put("min", min);
                typeStat.put("count", values.size());
                features.put(type, typeStat);
            }
        }

        try {
            return objectMapper.writeValueAsString(features);
        } catch (Exception e) {
            log.error("特征提取序列化失败: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 根据 AI 结果构建评测报告
     */
    private Assessment buildAssessment(Long userId, String aiResult) {
        BigDecimal overallScore = new BigDecimal("75.00");
        String riskLevel = "LOW";
        String summary = "健康状况良好";
        String recommendation = "保持规律作息，坚持适量运动";

        try {
            JsonNode resultNode = objectMapper.readTree(aiResult);

            if (resultNode.has("healthScore")) {
                overallScore = new BigDecimal(resultNode.get("healthScore").asText("75"));
            }

            if (resultNode.has("diseaseRiskLevel")) {
                riskLevel = resultNode.get("diseaseRiskLevel").asText("LOW");
            }

            if (resultNode.has("summary")) {
                summary = resultNode.get("summary").asText();
            }

            if (resultNode.has("recommendations")) {
                StringBuilder sb = new StringBuilder();
                JsonNode recs = resultNode.get("recommendations");
                if (recs.isArray()) {
                    for (int i = 0; i < recs.size(); i++) {
                        if (i > 0) sb.append("\n");
                        sb.append(i + 1).append(". ").append(recs.get(i).asText());
                    }
                    recommendation = sb.toString();
                }
            }

        } catch (Exception e) {
            log.warn("解析 AI 结果失败，使用默认值: {}", e.getMessage());
        }

        return Assessment.builder()
                .userId(userId)
                .assessmentDate(LocalDate.now())
                .overallScore(overallScore)
                .riskLevel(riskLevel)
                .summary(summary)
                .recommendation(recommendation)
                .aiAnalysis(aiResult)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 发送评测完成通知
     */
    private void sendAssessmentNotification(Long userId, Assessment assessment) {
        try {
            String content = String.format(
                    "您的健康评测已完成！健康评分: %.1f分，风险等级: %s。请查看详细报告。",
                    assessment.getOverallScore().doubleValue(),
                    riskLevelLabel(assessment.getRiskLevel())
            );

            Notification notification = Notification.builder()
                    .userId(userId)
                    .notificationType("assessment")
                    .title("健康评测报告已生成")
                    .content(content)
                    .readStatus(0)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationMapper.insert(notification);
        } catch (Exception e) {
            log.warn("发送评测通知失败: {}", e.getMessage());
        }
    }

    private String riskLevelLabel(String riskLevel) {
        switch (riskLevel) {
            case "LOW": return "低风险";
            case "MEDIUM": return "中风险";
            case "HIGH": return "高风险";
            case "CRITICAL": return "极高风险";
            default: return riskLevel;
        }
    }
}

