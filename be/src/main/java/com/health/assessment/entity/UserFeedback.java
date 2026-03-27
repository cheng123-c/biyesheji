package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户反馈实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFeedback implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 反馈类型（BUG/SUGGESTION/QUESTION/PRAISE/OTHER） */
    private String feedbackType;

    /** 反馈标题 */
    private String feedbackTitle;

    /** 反馈内容 */
    private String feedbackContent;

    /** 联系方式（可选） */
    private String contactInfo;

    /** 状态（PENDING/PROCESSING/RESOLVED/CLOSED） */
    private String status;

    /** 管理员回复 */
    private String adminReply;

    /** 回复时间 */
    private LocalDateTime repliedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 用户名（关联字段，非数据库列） */
    private String username;
}

