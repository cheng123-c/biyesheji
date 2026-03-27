package com.health.assessment.mapper;

import com.health.assessment.entity.UserFeedback;
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
 * 用户反馈 Mapper
 */
@Mapper
public interface UserFeedbackMapper {

    @Select("SELECT f.*, u.username FROM t_user_feedback f " +
            "LEFT JOIN t_user u ON f.user_id = u.id " +
            "WHERE f.id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "feedback_type", property = "feedbackType"),
            @Result(column = "feedback_title", property = "feedbackTitle"),
            @Result(column = "feedback_content", property = "feedbackContent"),
            @Result(column = "contact_info", property = "contactInfo"),
            @Result(column = "status", property = "status"),
            @Result(column = "admin_reply", property = "adminReply"),
            @Result(column = "replied_at", property = "repliedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "username", property = "username")
    })
    UserFeedback selectById(Long id);

    @Select("SELECT f.*, u.username FROM t_user_feedback f " +
            "LEFT JOIN t_user u ON f.user_id = u.id " +
            "WHERE f.user_id = #{userId} ORDER BY f.created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "feedback_type", property = "feedbackType"),
            @Result(column = "feedback_title", property = "feedbackTitle"),
            @Result(column = "feedback_content", property = "feedbackContent"),
            @Result(column = "contact_info", property = "contactInfo"),
            @Result(column = "status", property = "status"),
            @Result(column = "admin_reply", property = "adminReply"),
            @Result(column = "replied_at", property = "repliedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "username", property = "username")
    })
    List<UserFeedback> selectByUserId(Long userId);

    @Select("SELECT f.*, u.username FROM t_user_feedback f " +
            "LEFT JOIN t_user u ON f.user_id = u.id " +
            "ORDER BY f.created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "feedback_type", property = "feedbackType"),
            @Result(column = "feedback_title", property = "feedbackTitle"),
            @Result(column = "feedback_content", property = "feedbackContent"),
            @Result(column = "contact_info", property = "contactInfo"),
            @Result(column = "status", property = "status"),
            @Result(column = "admin_reply", property = "adminReply"),
            @Result(column = "replied_at", property = "repliedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "username", property = "username")
    })
    List<UserFeedback> selectAll();

    @Select("SELECT f.*, u.username FROM t_user_feedback f " +
            "LEFT JOIN t_user u ON f.user_id = u.id " +
            "WHERE f.status = #{status} ORDER BY f.created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "feedback_type", property = "feedbackType"),
            @Result(column = "feedback_title", property = "feedbackTitle"),
            @Result(column = "feedback_content", property = "feedbackContent"),
            @Result(column = "contact_info", property = "contactInfo"),
            @Result(column = "status", property = "status"),
            @Result(column = "admin_reply", property = "adminReply"),
            @Result(column = "replied_at", property = "repliedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "username", property = "username")
    })
    List<UserFeedback> selectByStatus(String status);

    @Insert("INSERT INTO t_user_feedback(user_id, feedback_type, feedback_title, feedback_content, contact_info, status) " +
            "VALUES(#{userId}, #{feedbackType}, #{feedbackTitle}, #{feedbackContent}, #{contactInfo}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserFeedback feedback);

    @Update("UPDATE t_user_feedback SET status=#{status}, admin_reply=#{adminReply}, replied_at=NOW() " +
            "WHERE id=#{id}")
    int updateReply(UserFeedback feedback);

    @Update("UPDATE t_user_feedback SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM t_user_feedback WHERE status = 'PENDING'")
    Integer countPending();
}

