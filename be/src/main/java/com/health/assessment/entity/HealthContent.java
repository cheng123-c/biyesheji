package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康内容库实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthContent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 内容标题 */
    private String contentTitle;

    /** 内容类型（article/video/guide） */
    private String contentType;

    /** 内容正文 */
    private String contentBody;

    /** 相关疾病（JSON数组） */
    private String relatedDiseases;

    /** 内容来源 */
    private String contentSource;

    /** 作者 */
    private String author;

    /** 是否发布 */
    private Integer isPublished;

    /** 发布时间 */
    private LocalDateTime publishedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}

