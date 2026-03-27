package com.health.assessment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 健康建议 DTO
 */
@Data
public class HealthSuggestionDTO {

    /**
     * 建议类型（DIET/EXERCISE/MEDICATION/LIFESTYLE/OTHER）
     */
    private String suggestionType;

    /**
     * 建议内容
     */
    @NotBlank(message = "建议内容不能为空")
    private String suggestionContent;

    /**
     * 优先级（LOW/MEDIUM/HIGH）
     */
    private String priority;

    /**
     * 循证等级
     */
    private String evidenceLevel;
}

