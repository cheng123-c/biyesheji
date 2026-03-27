package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医疗记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord implements Serializable {

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
     * 记录类型（DIAGNOSIS/MEDICATION/SURGERY/EXAMINATION/OTHER）
     */
    private String recordType;

    /**
     * 记录标题
     */
    private String recordTitle;

    /**
     * 记录内容
     */
    private String recordContent;

    /**
     * 记录日期
     */
    private LocalDate recordDate;

    /**
     * 医院名称
     */
    private String hospital;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}

