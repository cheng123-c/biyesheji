package com.health.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识图谱关系实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sourceConceptId;
    private Long targetConceptId;
    /** HAS_SYMPTOM / INDICATES / CAUSES / TREATED_BY */
    private String relationType;
    private BigDecimal confidenceScore;
    private LocalDateTime createdAt;

    // 关联字段
    private String sourceConceptName;
    private String targetConceptName;
    private String sourceConceptType;
    private String targetConceptType;
}

