package com.health.assessment.mapper;

import com.health.assessment.entity.InterventionPlan;
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
 * 干预方案 Mapper 接口
 */
@Mapper
public interface InterventionPlanMapper {

    /**
     * 根据ID查询干预方案
     */
    @Select("SELECT * FROM t_intervention_plan WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "plan_type", property = "planType"),
            @Result(column = "target_disease", property = "targetDisease"),
            @Result(column = "plan_detail", property = "planDetail"),
            @Result(column = "duration_days", property = "durationDays"),
            @Result(column = "start_date", property = "startDate"),
            @Result(column = "end_date", property = "endDate"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt")
    })
    InterventionPlan selectById(Long id);

    /**
     * 查询用户所有干预方案（按创建时间降序）
     */
    @Select("SELECT * FROM t_intervention_plan WHERE user_id = #{userId} ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "plan_type", property = "planType"),
            @Result(column = "target_disease", property = "targetDisease"),
            @Result(column = "plan_detail", property = "planDetail"),
            @Result(column = "duration_days", property = "durationDays"),
            @Result(column = "start_date", property = "startDate"),
            @Result(column = "end_date", property = "endDate"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<InterventionPlan> selectByUserId(Long userId);

    /**
     * 按状态查询用户干预方案
     */
    @Select("SELECT * FROM t_intervention_plan WHERE user_id = #{userId} AND status = #{status} ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "plan_type", property = "planType"),
            @Result(column = "target_disease", property = "targetDisease"),
            @Result(column = "plan_detail", property = "planDetail"),
            @Result(column = "duration_days", property = "durationDays"),
            @Result(column = "start_date", property = "startDate"),
            @Result(column = "end_date", property = "endDate"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<InterventionPlan> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 查询用户进行中的干预方案
     */
    @Select("SELECT * FROM t_intervention_plan WHERE user_id = #{userId} AND status = 'ACTIVE' ORDER BY start_date ASC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "plan_type", property = "planType"),
            @Result(column = "target_disease", property = "targetDisease"),
            @Result(column = "plan_detail", property = "planDetail"),
            @Result(column = "duration_days", property = "durationDays"),
            @Result(column = "start_date", property = "startDate"),
            @Result(column = "end_date", property = "endDate"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<InterventionPlan> selectActiveByUserId(Long userId);

    /**
     * 新增干预方案
     */
    @Insert("INSERT INTO t_intervention_plan(user_id, plan_type, target_disease, plan_detail, duration_days, start_date, end_date, status) " +
            "VALUES(#{userId}, #{planType}, #{targetDisease}, #{planDetail}, #{durationDays}, #{startDate}, #{endDate}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(InterventionPlan plan);

    /**
     * 更新干预方案
     */
    @Update("UPDATE t_intervention_plan SET plan_type = #{planType}, target_disease = #{targetDisease}, " +
            "plan_detail = #{planDetail}, duration_days = #{durationDays}, start_date = #{startDate}, " +
            "end_date = #{endDate}, status = #{status} WHERE id = #{id} AND user_id = #{userId}")
    int update(InterventionPlan plan);

    /**
     * 更新干预方案状态
     */
    @Update("UPDATE t_intervention_plan SET status = #{status} WHERE id = #{id} AND user_id = #{userId}")
    int updateStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") String status);

    /**
     * 删除干预方案
     */
    @Delete("DELETE FROM t_intervention_plan WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 统计用户进行中的干预方案数量
     */
    @Select("SELECT COUNT(*) FROM t_intervention_plan WHERE user_id = #{userId} AND status = 'ACTIVE'")
    Integer countActiveByUserId(Long userId);

    /**
     * 统计用户干预方案总数
     */
    @Select("SELECT COUNT(*) FROM t_intervention_plan WHERE user_id = #{userId}")
    Integer countByUserId(Long userId);

    /**
     * 分页查询用户干预方案（支持 planType/status 过滤）
     * （定义在 InterventionPlanMapper.xml 中，使用 resultMap 映射字段）
     */
    List<InterventionPlan> selectByUserIdWithPage(@Param("userId") Long userId,
                                                  @Param("planType") String planType,
                                                  @Param("status") String status,
                                                  @Param("offset") int offset,
                                                  @Param("limit") int limit);

    /**
     * 统计用户干预方案数量（支持 planType/status 过滤）
     * （定义在 InterventionPlanMapper.xml 中）
     */
    Integer countByUserIdWithCondition(@Param("userId") Long userId,
                                       @Param("planType") String planType,
                                       @Param("status") String status);

    /**
     * 查询即将到期的干预方案
     * （定义在 InterventionPlanMapper.xml 中，使用 resultMap 映射字段）
     */
    List<InterventionPlan> selectExpiringSoon(@Param("daysAhead") int daysAhead);

    /**
     * 按状态和类型统计干预方案数量
     * （定义在 InterventionPlanMapper.xml 中）
     */
    List<java.util.Map<String, Object>> countByStatusAndType(@Param("userId") Long userId);

    /**
     * 批量将已到期方案状态更新为 COMPLETED
     * （定义在 InterventionPlanMapper.xml 中）
     */
    int batchExpirePlans();
}

