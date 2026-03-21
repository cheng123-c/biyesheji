package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康数据实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthData implements Serializable {

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
     * 数据类型（heart_rate, blood_pressure_systolic等）
     */
    private String dataType;

    /**
     * 数据值
     */
    private BigDecimal dataValue;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数据来源（device, manual, import等）
     */
    private String dataSource;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 采集时间
     */
    private LocalDateTime collectedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}

