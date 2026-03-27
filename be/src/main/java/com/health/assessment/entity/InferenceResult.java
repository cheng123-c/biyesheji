package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识推理结果实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InferenceResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    /** 输入症状（JSON数组） */
    private String inputSymptoms;
    /** 推理出的潜在疾病（JSON数组） */
    private String inferredDiseases;
    /** AI分析建议 */
    private String aiAnalysis;
    /** 综合风险评分 */
    private BigDecimal riskScore;
    private LocalDateTime createdAt;
}

