package com.health.assessment.mapper;

import com.health.assessment.entity.InferenceResult;
import com.health.assessment.entity.KnowledgeRelation;
import com.health.assessment.entity.MedicalConcept;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 知识图谱 Mapper
 */
@Mapper
public interface KnowledgeMapper {

    // ===== 医学概念 =====

    @Select("SELECT * FROM t_medical_concept WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "concept_name", property = "conceptName"),
            @Result(column = "concept_type", property = "conceptType"),
            @Result(column = "description", property = "description"),
            @Result(column = "icd10_code", property = "icd10Code"),
            @Result(column = "created_at", property = "createdAt")
    })
    MedicalConcept selectConceptById(Long id);

    @Select("SELECT * FROM t_medical_concept WHERE concept_type = #{conceptType} ORDER BY concept_name")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "concept_name", property = "conceptName"),
            @Result(column = "concept_type", property = "conceptType"),
            @Result(column = "description", property = "description"),
            @Result(column = "icd10_code", property = "icd10Code"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<MedicalConcept> selectConceptsByType(String conceptType);

    @Select("SELECT * FROM t_medical_concept WHERE concept_name LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY concept_name LIMIT 30")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "concept_name", property = "conceptName"),
            @Result(column = "concept_type", property = "conceptType"),
            @Result(column = "description", property = "description"),
            @Result(column = "icd10_code", property = "icd10Code"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<MedicalConcept> searchConcepts(String keyword);

    @Select("SELECT * FROM t_medical_concept ORDER BY concept_type, concept_name")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "concept_name", property = "conceptName"),
            @Result(column = "concept_type", property = "conceptType"),
            @Result(column = "description", property = "description"),
            @Result(column = "icd10_code", property = "icd10Code"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<MedicalConcept> selectAllConcepts();

    @Insert("INSERT INTO t_medical_concept(concept_name, concept_type, description, icd10_code) " +
            "VALUES(#{conceptName}, #{conceptType}, #{description}, #{icd10Code})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertConcept(MedicalConcept concept);

    @Update("UPDATE t_medical_concept SET concept_name=#{conceptName}, concept_type=#{conceptType}, " +
            "description=#{description}, icd10_code=#{icd10Code} WHERE id=#{id}")
    int updateConcept(MedicalConcept concept);

    @Delete("DELETE FROM t_medical_concept WHERE id = #{id}")
    int deleteConceptById(Long id);

    // ===== 知识关系 =====

    @Select("SELECT r.*, " +
            "sc.concept_name AS source_concept_name, sc.concept_type AS source_concept_type, " +
            "tc.concept_name AS target_concept_name, tc.concept_type AS target_concept_type " +
            "FROM t_knowledge_relation r " +
            "LEFT JOIN t_medical_concept sc ON r.source_concept_id = sc.id " +
            "LEFT JOIN t_medical_concept tc ON r.target_concept_id = tc.id " +
            "WHERE r.source_concept_id = #{conceptId} OR r.target_concept_id = #{conceptId}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "source_concept_id", property = "sourceConceptId"),
            @Result(column = "target_concept_id", property = "targetConceptId"),
            @Result(column = "relation_type", property = "relationType"),
            @Result(column = "confidence_score", property = "confidenceScore"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "source_concept_name", property = "sourceConceptName"),
            @Result(column = "source_concept_type", property = "sourceConceptType"),
            @Result(column = "target_concept_name", property = "targetConceptName"),
            @Result(column = "target_concept_type", property = "targetConceptType")
    })
    List<KnowledgeRelation> selectRelationsByConceptId(Long conceptId);

    /**
     * 根据一组症状概念ID，查询关联的疾病（通过 HAS_SYMPTOM 关系反向查找）
     */
    @Select("<script>" +
            "SELECT r.*, " +
            "sc.concept_name AS source_concept_name, sc.concept_type AS source_concept_type, " +
            "tc.concept_name AS target_concept_name, tc.concept_type AS target_concept_type " +
            "FROM t_knowledge_relation r " +
            "LEFT JOIN t_medical_concept sc ON r.source_concept_id = sc.id " +
            "LEFT JOIN t_medical_concept tc ON r.target_concept_id = tc.id " +
            "WHERE r.relation_type = 'HAS_SYMPTOM' " +
            "AND r.target_concept_id IN " +
            "<foreach item='id' collection='symptomIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY r.confidence_score DESC" +
            "</script>")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "source_concept_id", property = "sourceConceptId"),
            @Result(column = "target_concept_id", property = "targetConceptId"),
            @Result(column = "relation_type", property = "relationType"),
            @Result(column = "confidence_score", property = "confidenceScore"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "source_concept_name", property = "sourceConceptName"),
            @Result(column = "source_concept_type", property = "sourceConceptType"),
            @Result(column = "target_concept_name", property = "targetConceptName"),
            @Result(column = "target_concept_type", property = "targetConceptType")
    })
    List<KnowledgeRelation> selectDiseasesBySymptomIds(@Param("symptomIds") List<Long> symptomIds);

    @Insert("INSERT INTO t_knowledge_relation(source_concept_id, target_concept_id, relation_type, confidence_score) " +
            "VALUES(#{sourceConceptId}, #{targetConceptId}, #{relationType}, #{confidenceScore})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRelation(KnowledgeRelation relation);

    @Delete("DELETE FROM t_knowledge_relation WHERE id = #{id}")
    int deleteRelationById(Long id);

    // ===== 推理结果 =====

    @Select("SELECT * FROM t_inference_result WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "input_symptoms", property = "inputSymptoms"),
            @Result(column = "inferred_diseases", property = "inferredDiseases"),
            @Result(column = "ai_analysis", property = "aiAnalysis"),
            @Result(column = "risk_score", property = "riskScore"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<InferenceResult> selectInferenceResultsByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Insert("INSERT INTO t_inference_result(user_id, input_symptoms, inferred_diseases, ai_analysis, risk_score) " +
            "VALUES(#{userId}, #{inputSymptoms}, #{inferredDiseases}, #{aiAnalysis}, #{riskScore})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertInferenceResult(InferenceResult result);
}

