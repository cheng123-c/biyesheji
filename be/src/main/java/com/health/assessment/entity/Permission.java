package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 权限代码
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 操作类型（view/create/update/delete）
     */
    private String operation;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}

