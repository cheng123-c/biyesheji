package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 评测报告实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评测日期
     */
    private LocalDate assessmentDate;

    /**
     * 整体评分
     */
    private BigDecimal overallScore;

    /**
     * 风险等级（LOW/MEDIUM/HIGH/CRITICAL）
     */
    private String riskLevel;

    /**
     * 报告摘要
     */
    private String summary;

    /**
     * 健康建议
     */
    private String recommendation;

    /**
     * AI 分析（JSON格式）
     */
    private String aiAnalysis;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}

