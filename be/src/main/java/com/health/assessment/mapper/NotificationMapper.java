package com.health.assessment.mapper;

import com.health.assessment.entity.Notification;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统通知 Mapper 接口
 */
@Mapper
public interface NotificationMapper {

    /**
     * 根据ID查询通知
     */
    @Select("SELECT * FROM t_notification WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "notification_type", property = "notificationType"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "read_status", property = "readStatus"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "read_at", property = "readAt")
    })
    Notification selectById(Long id);

    /**
     * 查询用户的所有通知
     */
    @Select("SELECT * FROM t_notification WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "notification_type", property = "notificationType"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "read_status", property = "readStatus"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "read_at", property = "readAt")
    })
    List<Notification> selectByUserId(Long userId);

    /**
     * 查询用户的未读通知
     */
    @Select("SELECT * FROM t_notification WHERE user_id = #{userId} " +
            "AND read_status = 0 ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "notification_type", property = "notificationType"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "read_status", property = "readStatus"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "read_at", property = "readAt")
    })
    List<Notification> selectUnreadByUserId(Long userId);

    /**
     * 查询用户的未读通知数量
     */
    @Select("SELECT COUNT(*) FROM t_notification WHERE user_id = #{userId} " +
            "AND read_status = 0")
    Integer countUnreadByUserId(Long userId);

    /**
     * 查询用户特定类型的通知
     */
    @Select("SELECT * FROM t_notification WHERE user_id = #{userId} " +
            "AND notification_type = #{notificationType} " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "notification_type", property = "notificationType"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "read_status", property = "readStatus"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "read_at", property = "readAt")
    })
    List<Notification> selectByUserIdAndType(@Param("userId") Long userId,
                                             @Param("notificationType") String notificationType,
                                             @Param("limit") Integer limit);

    /**
     * 查询指定时间之后的通知
     */
    @Select("SELECT * FROM t_notification WHERE user_id = #{userId} " +
            "AND created_at >= #{afterTime} ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "notification_type", property = "notificationType"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "read_status", property = "readStatus"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "read_at", property = "readAt")
    })
    List<Notification> selectAfterTime(@Param("userId") Long userId,
                                       @Param("afterTime") LocalDateTime afterTime);

    /**
     * 新增通知
     */
    @Insert("INSERT INTO t_notification(user_id, notification_type, title, content, read_status) " +
            "VALUES(#{userId}, #{notificationType}, #{title}, #{content}, #{readStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    /**
     * 批量插入通知
     */
    @Insert("<script>" +
            "INSERT INTO t_notification(user_id, notification_type, title, content, read_status) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.userId}, #{item.notificationType}, #{item.title}, #{item.content}, #{item.readStatus})" +
            "</foreach>" +
            "</script>")
    int batchInsert(List<Notification> notificationList);

    /**
     * 标记通知为已读
     */
    @Update("UPDATE t_notification SET read_status = 1, read_at = NOW() " +
            "WHERE id = #{id}")
    int markAsRead(Long id);

    /**
     * 标记用户的所有通知为已读
     */
    @Update("UPDATE t_notification SET read_status = 1, read_at = NOW() " +
            "WHERE user_id = #{userId}")
    int markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    @Delete("DELETE FROM t_notification WHERE id = #{id}")
    int delete(Long id);

    /**
     * 删除用户的所有通知
     */
    @Delete("DELETE FROM t_notification WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    /**
     * 删除指定时间之前的通知（数据清理）
     */
    @Delete("DELETE FROM t_notification WHERE created_at < #{beforeTime}")
    int deleteOldNotifications(LocalDateTime beforeTime);

    /**
     * 分页查询用户通知（支持 notificationType/readStatus 过滤）
     * （定义在 NotificationMapper.xml 中，使用 resultMap 映射字段）
     */
    List<Notification> selectByUserIdWithPage(@Param("userId") Long userId,
                                              @Param("notificationType") String notificationType,
                                              @Param("readStatus") Integer readStatus,
                                              @Param("offset") int offset,
                                              @Param("limit") int limit);

    /**
     * 统计用户通知数量（支持 notificationType/readStatus 过滤）
     * （定义在 NotificationMapper.xml 中）
     */
    Integer countByUserId(@Param("userId") Long userId,
                          @Param("notificationType") String notificationType,
                          @Param("readStatus") Integer readStatus);

    /**
     * 批量标记通知为已读
     * （定义在 NotificationMapper.xml 中）
     */
    int batchMarkAsRead(@Param("ids") List<Long> ids);

    /**
     * 按类型统计通知数量及未读数
     * （定义在 NotificationMapper.xml 中）
     */
    List<java.util.Map<String, Object>> countByType(@Param("userId") Long userId);

}

