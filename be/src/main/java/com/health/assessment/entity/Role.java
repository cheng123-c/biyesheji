package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色状态（ACTIVE/INACTIVE）
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}

