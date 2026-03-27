package com.health.assessment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 医疗记录请求 DTO
 */
@Data
public class MedicalRecordDTO {

    /**
     * 记录类型（DIAGNOSIS/MEDICATION/SURGERY/EXAMINATION/OTHER）
     */
    @NotBlank(message = "记录类型不能为空")
    private String recordType;

    /**
     * 记录标题
     */
    @NotBlank(message = "记录标题不能为空")
    @Size(max = 200, message = "标题不能超过200个字符")
    private String recordTitle;

    /**
     * 记录内容
     */
    private String recordContent;

    /**
     * 记录日期
     */
    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;

    /**
     * 医院名称
     */
    @Size(max = 200, message = "医院名称不能超过200个字符")
    private String hospital;

    /**
     * 医生姓名
     */
    @Size(max = 100, message = "医生姓名不能超过100个字符")
    private String doctorName;
}

