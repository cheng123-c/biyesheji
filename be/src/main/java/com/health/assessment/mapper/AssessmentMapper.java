package com.health.assessment.mapper;

import com.health.assessment.entity.Assessment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 评测报告 Mapper 接口
 */
@Mapper
public interface AssessmentMapper {

    /**
     * 根据ID查询评测报告
     */
    @Select("SELECT * FROM t_assessment_report WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "assessment_date", property = "assessmentDate"),
            @Result(column = "overall_score", property = "overallScore"),
            @Result(column = "risk_level", property = "riskLevel"),
            @Result(column = "summary", property = "summary"),
            @Result(column = "recommendation", property = "recommendation"),
            @Result(column = "ai_analysis", property = "aiAnalysis"),
            @Result(column = "created_at", property = "createdAt")
    })
    Assessment selectById(Long id);

    /**
     * 根据用户ID查询最新的评测报告
     */
    @Select("SELECT * FROM t_assessment_report WHERE user_id = #{userId} " +
            "ORDER BY assessment_date DESC LIMIT 1")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "assessment_date", property = "assessmentDate"),
            @Result(column = "overall_score", property = "overallScore"),
            @Result(column = "risk_level", property = "riskLevel"),
            @Result(column = "summary", property = "summary"),
            @Result(column = "recommendation", property = "recommendation"),
            @Result(column = "ai_analysis", property = "aiAnalysis"),
            @Result(column = "created_at", property = "createdAt")
    })
    Assessment selectLatestByUserId(Long userId);

    /**
     * 根据用户ID和评测日期查询报告
     */
    @Select("SELECT * FROM t_assessment_report WHERE user_id = #{userId} " +
            "AND assessment_date = #{assessmentDate}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "assessment_date", property = "assessmentDate"),
            @Result(column = "overall_score", property = "overallScore"),
            @Result(column = "risk_level", property = "riskLevel"),
            @Result(column = "summary", property = "summary"),
            @Result(column = "recommendation", property = "recommendation"),
            @Result(column = "ai_analysis", property = "aiAnalysis"),
            @Result(column = "created_at", property = "createdAt")
    })
    Assessment selectByUserIdAndDate(@Param("userId") Long userId,
                                     @Param("assessmentDate") LocalDate assessmentDate);

    /**
     * 查询用户的所有评测报告
     */
    @Select("SELECT * FROM t_assessment_report WHERE user_id = #{userId} " +
            "ORDER BY assessment_date DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "assessment_date", property = "assessmentDate"),
            @Result(column = "overall_score", property = "overallScore"),
            @Result(column = "risk_level", property = "riskLevel"),
            @Result(column = "summary", property = "summary"),
            @Result(column = "recommendation", property = "recommendation"),
            @Result(column = "ai_analysis", property = "aiAnalysis"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Assessment> selectByUserId(Long userId);

    /**
     * 查询用户指定时间范围内的评测报告
     */
    @Select("SELECT * FROM t_assessment_report WHERE user_id = #{userId} " +
            "AND assessment_date >= #{startDate} AND assessment_date <= #{endDate} " +
            "ORDER BY assessment_date DESC")
    List<Assessment> selectByUserIdAndDateRange(@Param("userId") Long userId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * 查询高风险用户的报告
     */
    @Select("SELECT * FROM t_assessment_report " +
            "WHERE risk_level IN ('HIGH', 'CRITICAL') " +
            "AND assessment_date >= #{afterDate} " +
            "ORDER BY assessment_date DESC")
    List<Assessment> selectHighRiskReports(@Param("afterDate") LocalDate afterDate);

    /**
     * 新增评测报告
     */
    @Insert("INSERT INTO t_assessment_report(user_id, assessment_date, overall_score, risk_level, " +
            "summary, recommendation, ai_analysis) " +
            "VALUES(#{userId}, #{assessmentDate}, #{overallScore}, #{riskLevel}, " +
            "#{summary}, #{recommendation}, #{aiAnalysis})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Assessment assessment);

    /**
     * 更新评测报告
     */
    @Update("UPDATE t_assessment_report SET overall_score = #{overallScore}, " +
            "risk_level = #{riskLevel}, summary = #{summary}, " +
            "recommendation = #{recommendation}, ai_analysis = #{aiAnalysis} " +
            "WHERE id = #{id}")
    int update(Assessment assessment);

    /**
     * 删除评测报告
     */
    @Delete("DELETE FROM t_assessment_report WHERE id = #{id}")
    int delete(Long id);

    /**
     * 查询用户今年的评测次数
     */
    @Select("SELECT COUNT(*) FROM t_assessment_report " +
            "WHERE user_id = #{userId} AND YEAR(assessment_date) = YEAR(NOW())")
    Integer countCurrentYearByUserId(Long userId);

}

