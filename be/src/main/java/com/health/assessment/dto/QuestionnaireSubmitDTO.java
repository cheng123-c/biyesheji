package com.health.assessment.dto;

import lombok.Data;

import java.util.Map;
import javax.validation.constraints.NotNull;

/**
 * 提交问卷回答 DTO
 */
@Data
public class QuestionnaireSubmitDTO {

    /** 问卷ID */
    @NotNull(message = "问卷ID不能为空")
    private Long questionnaireId;

    /**
     * 问卷回答数据，key为题目ID（字符串），value为答案
     * 例如：{"q1": "A", "q2": ["B","C"], "q3": "每天运动30分钟"}
     */
    @NotNull(message = "回答数据不能为空")
    private Map<String, Object> answers;
}

