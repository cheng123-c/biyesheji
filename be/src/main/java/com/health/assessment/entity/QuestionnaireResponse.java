package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问卷回答实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 问卷ID */
    private Long questionnaireId;

    /** 问卷回答数据（JSON） */
    private String responseData;

    /** 问卷得分 */
    private java.math.BigDecimal score;

    /** 完成时间 */
    private LocalDateTime completedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    // ---- 关联字段（非数据库列） ----

    /** 问卷标题（查询时关联） */
    private String questionnaireTitle;

    /** 问卷类型 */
    private String questionnaireType;
}

