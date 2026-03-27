package com.health.assessment.mapper;

import com.health.assessment.entity.HealthSuggestion;
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
 * 健康建议 Mapper 接口
 */
@Mapper
public interface HealthSuggestionMapper {

    /**
     * 根据ID查询健康建议
     */
    @Select("SELECT * FROM t_health_suggestion WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "suggestion_type", property = "suggestionType"),
            @Result(column = "suggestion_content", property = "suggestionContent"),
            @Result(column = "priority", property = "priority"),
            @Result(column = "evidence_level", property = "evidenceLevel"),
            @Result(column = "created_by", property = "createdBy"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "read_at", property = "readAt")
    })
    HealthSuggestion selectById(Long id);

    /**
     * 查询用户的所有健康建议（按创建时间降序）
     */
    @Select("SELECT * FROM t_health_suggestion WHERE user_id = #{userId} ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "suggestion_type", property = "suggestionType"),
            @Result(column = "suggestion_content", property = "suggestionContent"),
            @Result(column = "priority", property = "priority"),
            @Result(column = "evidence_level", property = "evidenceLevel"),
            @Result(column = "created_by", property = "createdBy"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "read_at", property = "readAt")
    })
    List<HealthSuggestion> selectByUserId(Long userId);

    /**
     * 查询用户未读的健康建议
     */
    @Select("SELECT * FROM t_health_suggestion WHERE user_id = #{userId} AND read_at IS NULL ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "suggestion_type", property = "suggestionType"),
            @Result(column = "suggestion_content", property = "suggestionContent"),
            @Result(column = "priority", property = "priority"),
            @Result(column = "evidence_level", property = "evidenceLevel"),
            @Result(column = "created_by", property = "createdBy"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "read_at", property = "readAt")
    })
    List<HealthSuggestion> selectUnreadByUserId(Long userId);

    /**
     * 按优先级查询用户健康建议
     */
    @Select("SELECT * FROM t_health_suggestion WHERE user_id = #{userId} AND priority = #{priority} ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "suggestion_type", property = "suggestionType"),
            @Result(column = "suggestion_content", property = "suggestionContent"),
            @Result(column = "priority", property = "priority"),
            @Result(column = "evidence_level", property = "evidenceLevel"),
            @Result(column = "created_by", property = "createdBy"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "read_at", property = "readAt")
    })
    List<HealthSuggestion> selectByUserIdAndPriority(@Param("userId") Long userId, @Param("priority") String priority);

    /**
     * 新增健康建议
     */
    @Insert("INSERT INTO t_health_suggestion(user_id, suggestion_type, suggestion_content, priority, evidence_level, created_by, created_at) " +
            "VALUES(#{userId}, #{suggestionType}, #{suggestionContent}, #{priority}, #{evidenceLevel}, #{createdBy}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HealthSuggestion suggestion);

    /**
     * 标记建议为已读
     */
    @Update("UPDATE t_health_suggestion SET read_at = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 标记用户所有建议为已读
     */
    @Update("UPDATE t_health_suggestion SET read_at = NOW() WHERE user_id = #{userId} AND read_at IS NULL")
    int markAllAsRead(Long userId);

    /**
     * 删除健康建议
     */
    @Delete("DELETE FROM t_health_suggestion WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 统计用户未读建议数量
     */
    @Select("SELECT COUNT(*) FROM t_health_suggestion WHERE user_id = #{userId} AND read_at IS NULL")
    Integer countUnreadByUserId(Long userId);

    /**
     * 统计用户建议总数
     */
    @Select("SELECT COUNT(*) FROM t_health_suggestion WHERE user_id = #{userId}")
    Integer countByUserId(Long userId);
}

