package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康建议实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthSuggestion implements Serializable {

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
     * 建议类型（DIET/EXERCISE/MEDICATION/LIFESTYLE/OTHER）
     */
    private String suggestionType;

    /**
     * 建议内容
     */
    private String suggestionContent;

    /**
     * 优先级（LOW/MEDIUM/HIGH）
     */
    private String priority;

    /**
     * 循证等级
     */
    private String evidenceLevel;

    /**
     * 创建者ID（0表示AI生成）
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;
}

