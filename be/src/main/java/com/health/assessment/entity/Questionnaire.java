package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问卷定义实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Questionnaire implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 问卷标题 */
    private String title;

    /** 问卷描述 */
    private String description;

    /** 问卷类型（LIFESTYLE/SYMPTOM/MENTAL/DIET/EXERCISE） */
    private String questionnaireType;

    /** 问题列表（JSON数组） */
    private String questions;

    /** 是否启用 */
    private Integer isActive;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}

