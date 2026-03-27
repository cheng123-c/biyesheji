package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 干预方案实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterventionPlan implements Serializable {

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
     * 方案类型（DIET/EXERCISE/MEDICATION/REHABILITATION/OTHER）
     */
    private String planType;

    /**
     * 目标疾病/目标
     */
    private String targetDisease;

    /**
     * 方案详情（JSON格式）
     */
    private String planDetail;

    /**
     * 持续天数
     */
    private Integer durationDays;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 状态（ACTIVE/PAUSED/COMPLETED/CANCELLED）
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

