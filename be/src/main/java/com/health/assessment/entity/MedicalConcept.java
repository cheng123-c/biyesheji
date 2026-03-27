package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医学知识节点实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalConcept implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String conceptName;
    /** DISEASE/SYMPTOM/INDICATOR/TREATMENT */
    private String conceptType;
    private String description;
    private String icd10Code;
    private LocalDateTime createdAt;
}

