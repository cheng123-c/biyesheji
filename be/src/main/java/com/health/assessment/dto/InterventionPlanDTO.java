package com.health.assessment.dto;

import lombok.Data;

import java.time.LocalDate;
import javax.validation.constraints.NotBlank;

/**
 * 干预方案 DTO
 */
@Data
public class InterventionPlanDTO {

    /**
     * 方案类型（DIET/EXERCISE/MEDICATION/REHABILITATION/OTHER）
     */
    @NotBlank(message = "方案类型不能为空")
    private String planType;

    /**
     * 目标疾病/目标
     */
    private String targetDisease;

    /**
     * 方案详情（JSON格式字符串）
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
}

