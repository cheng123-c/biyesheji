package com.health.assessment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 健康数据上传 DTO
 */
@Data
public class HealthDataUploadDTO {

    /**
     * 数据类型（heart_rate, blood_pressure_systolic, blood_glucose等）
     */
    @NotBlank(message = "数据类型不能为空")
    private String dataType;

    /**
     * 数据值
     */
    @NotNull(message = "数据值不能为空")
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
     * 采集时间（不填则使用当前时间）
     * 支持格式：
     *   - "yyyy-MM-dd HH:mm:ss" （如 "2024-03-22 12:00:00"）
     *   - "yyyy-MM-dd'T'HH:mm:ss" （如 "2024-03-22T12:00:00"，来自 HTML datetime-local）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectedAt;
}

