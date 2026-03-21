package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统通知实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification implements Serializable {

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
     * 通知类型（alert, suggestion, reminder等）
     */
    private String notificationType;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 阅读状态
     * 0 = 未读, 1 = 已读
     */
    private Integer readStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;

}

