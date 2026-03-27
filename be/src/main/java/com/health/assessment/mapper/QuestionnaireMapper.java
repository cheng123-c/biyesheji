package com.health.assessment.mapper;

import com.health.assessment.entity.Questionnaire;
import com.health.assessment.entity.QuestionnaireResponse;
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
 * 问卷 Mapper 接口
 */
@Mapper
public interface QuestionnaireMapper {

    // ==================== 问卷定义 ====================

    @Select("SELECT * FROM t_questionnaire WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "questionnaire_type", property = "questionnaireType"),
            @Result(column = "questions", property = "questions"),
            @Result(column = "is_active", property = "isActive"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    Questionnaire selectById(Long id);

    @Select("SELECT * FROM t_questionnaire WHERE is_active = 1 ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "questionnaire_type", property = "questionnaireType"),
            @Result(column = "questions", property = "questions"),
            @Result(column = "is_active", property = "isActive"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Questionnaire> selectAllActive();

    @Select("SELECT * FROM t_questionnaire ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "questionnaire_type", property = "questionnaireType"),
            @Result(column = "questions", property = "questions"),
            @Result(column = "is_active", property = "isActive"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Questionnaire> selectAll();

    @Select("SELECT * FROM t_questionnaire WHERE questionnaire_type = #{type} AND is_active = 1 ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "questionnaire_type", property = "questionnaireType"),
            @Result(column = "questions", property = "questions"),
            @Result(column = "is_active", property = "isActive"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Questionnaire> selectByType(String type);

    @Insert("INSERT INTO t_questionnaire(title, description, questionnaire_type, questions, is_active) " +
            "VALUES(#{title}, #{description}, #{questionnaireType}, #{questions}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Questionnaire questionnaire);

    @Update("UPDATE t_questionnaire SET title=#{title}, description=#{description}, " +
            "questionnaire_type=#{questionnaireType}, questions=#{questions}, " +
            "is_active=#{isActive}, updated_at=NOW() WHERE id=#{id}")
    int update(Questionnaire questionnaire);

    @Delete("DELETE FROM t_questionnaire WHERE id = #{id}")
    int deleteById(Long id);

    // ==================== 问卷回答 ====================

    @Select("SELECT r.*, q.title AS questionnaire_title, q.questionnaire_type " +
            "FROM t_questionnaire_response r " +
            "LEFT JOIN t_questionnaire q ON r.questionnaire_id = q.id " +
            "WHERE r.id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "questionnaire_id", property = "questionnaireId"),
            @Result(column = "response_data", property = "responseData"),
            @Result(column = "score", property = "score"),
            @Result(column = "completed_at", property = "completedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "questionnaire_title", property = "questionnaireTitle"),
            @Result(column = "questionnaire_type", property = "questionnaireType")
    })
    QuestionnaireResponse selectResponseById(Long id);

    @Select("SELECT r.*, q.title AS questionnaire_title, q.questionnaire_type " +
            "FROM t_questionnaire_response r " +
            "LEFT JOIN t_questionnaire q ON r.questionnaire_id = q.id " +
            "WHERE r.user_id = #{userId} ORDER BY r.completed_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "questionnaire_id", property = "questionnaireId"),
            @Result(column = "response_data", property = "responseData"),
            @Result(column = "score", property = "score"),
            @Result(column = "completed_at", property = "completedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "questionnaire_title", property = "questionnaireTitle"),
            @Result(column = "questionnaire_type", property = "questionnaireType")
    })
    List<QuestionnaireResponse> selectResponsesByUserId(Long userId);

    @Select("SELECT r.*, q.title AS questionnaire_title, q.questionnaire_type " +
            "FROM t_questionnaire_response r " +
            "LEFT JOIN t_questionnaire q ON r.questionnaire_id = q.id " +
            "WHERE r.user_id = #{userId} AND r.questionnaire_id = #{questionnaireId} " +
            "ORDER BY r.completed_at DESC LIMIT 1")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "questionnaire_id", property = "questionnaireId"),
            @Result(column = "response_data", property = "responseData"),
            @Result(column = "score", property = "score"),
            @Result(column = "completed_at", property = "completedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "questionnaire_title", property = "questionnaireTitle"),
            @Result(column = "questionnaire_type", property = "questionnaireType")
    })
    QuestionnaireResponse selectLatestResponseByUserAndQuestionnaire(
            @Param("userId") Long userId, @Param("questionnaireId") Long questionnaireId);

    @Insert("INSERT INTO t_questionnaire_response(user_id, questionnaire_id, response_data, score, completed_at) " +
            "VALUES(#{userId}, #{questionnaireId}, #{responseData}, #{score}, #{completedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertResponse(QuestionnaireResponse response);

    @Select("SELECT COUNT(*) FROM t_questionnaire_response WHERE user_id = #{userId}")
    Integer countResponsesByUserId(Long userId);
}

