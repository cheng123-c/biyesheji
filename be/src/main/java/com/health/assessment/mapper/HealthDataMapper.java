package com.health.assessment.mapper;

import com.health.assessment.entity.HealthData;
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
import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康数据 Mapper 接口
 */
@Mapper
public interface HealthDataMapper {

    /**
     * 根据ID查询健康数据
     */
    @Select("SELECT * FROM t_health_data WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "data_type", property = "dataType"),
            @Result(column = "data_value", property = "dataValue"),
            @Result(column = "unit", property = "unit"),
            @Result(column = "data_source", property = "dataSource"),
            @Result(column = "device_id", property = "deviceId"),
            @Result(column = "collected_at", property = "collectedAt"),
            @Result(column = "created_at", property = "createdAt")
    })
    HealthData selectById(Long id);

    /**
     * 根据用户ID查询最新的健康数据
     */
    @Select("SELECT * FROM t_health_data WHERE user_id = #{userId} " +
            "ORDER BY collected_at DESC LIMIT #{limit}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "data_type", property = "dataType"),
            @Result(column = "data_value", property = "dataValue"),
            @Result(column = "unit", property = "unit"),
            @Result(column = "data_source", property = "dataSource"),
            @Result(column = "device_id", property = "deviceId"),
            @Result(column = "collected_at", property = "collectedAt"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<HealthData> selectLatestByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 根据用户ID和数据类型查询历史数据
     */
    @Select("SELECT * FROM t_health_data WHERE user_id = #{userId} AND data_type = #{dataType} " +
            "AND collected_at >= #{startTime} AND collected_at <= #{endTime} " +
            "ORDER BY collected_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "data_type", property = "dataType"),
            @Result(column = "data_value", property = "dataValue"),
            @Result(column = "unit", property = "unit"),
            @Result(column = "data_source", property = "dataSource"),
            @Result(column = "device_id", property = "deviceId"),
            @Result(column = "collected_at", property = "collectedAt"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<HealthData> selectByUserIdAndType(@Param("userId") Long userId,
                                           @Param("dataType") String dataType,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户今天的特定类型数据
     */
    @Select("SELECT * FROM t_health_data WHERE user_id = #{userId} AND data_type = #{dataType} " +
            "AND DATE(collected_at) = #{date} " +
            "ORDER BY collected_at DESC")
    List<HealthData> selectTodayData(@Param("userId") Long userId,
                                     @Param("dataType") String dataType,
                                     @Param("date") LocalDate date);

    /**
     * 查询用户指定时间范围内的所有健康数据
     */
    @Select("SELECT * FROM t_health_data WHERE user_id = #{userId} " +
            "AND collected_at >= #{startTime} AND collected_at <= #{endTime} " +
            "ORDER BY collected_at DESC")
    List<HealthData> selectByUserIdAndTimeRange(@Param("userId") Long userId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 查询所有数据类型的最新值
     */
    @Select("SELECT * FROM t_health_data WHERE user_id = #{userId} " +
            "AND collected_at = (" +
            "SELECT MAX(collected_at) FROM t_health_data " +
            "WHERE user_id = #{userId} AND data_type = t_health_data.data_type" +
            ")")
    List<HealthData> selectLatestAllTypes(Long userId);

    /**
     * 新增健康数据
     */
    @Insert("INSERT INTO t_health_data(user_id, data_type, data_value, unit, data_source, device_id, collected_at) " +
            "VALUES(#{userId}, #{dataType}, #{dataValue}, #{unit}, #{dataSource}, #{deviceId}, #{collectedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HealthData healthData);

    /**
     * 批量插入健康数据
     */
    @Insert("<script>" +
            "INSERT INTO t_health_data(user_id, data_type, data_value, unit, data_source, device_id, collected_at) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.userId}, #{item.dataType}, #{item.dataValue}, #{item.unit}, #{item.dataSource}, #{item.deviceId}, #{item.collectedAt})" +
            "</foreach>" +
            "</script>")
    int batchInsert(List<HealthData> healthDataList);

    /**
     * 更新健康数据
     */
    @Update("UPDATE t_health_data SET data_value = #{dataValue}, unit = #{unit}, " +
            "data_source = #{dataSource}, device_id = #{deviceId}, collected_at = #{collectedAt} " +
            "WHERE id = #{id}")
    int update(HealthData healthData);

    /**
     * 删除健康数据
     */
    @Delete("DELETE FROM t_health_data WHERE id = #{id}")
    int delete(Long id);

    /**
     * 删除用户指定日期之前的健康数据（数据归档）
     */
    @Delete("DELETE FROM t_health_data WHERE user_id = #{userId} AND collected_at < #{beforeDate}")
    int deleteBeforeDate(@Param("userId") Long userId, @Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 统计用户的数据类型数量
     */
    @Select("SELECT COUNT(DISTINCT data_type) FROM t_health_data WHERE user_id = #{userId}")
    Integer countDataTypes(Long userId);

    /**
     * 查询用户的数据统计信息
     */
    @Select("SELECT data_type, COUNT(*) as count, " +
            "MIN(data_value) as min_value, MAX(data_value) as max_value, " +
            "AVG(data_value) as avg_value " +
            "FROM t_health_data WHERE user_id = #{userId} " +
            "GROUP BY data_type")
    List<HealthDataStatistics> getStatistics(Long userId);

    /**
     * 健康数据统计对象（用于结果映射）
     */
    class HealthDataStatistics {
        public String dataType;
        public Long count;
        public java.math.BigDecimal minValue;
        public java.math.BigDecimal maxValue;
        public java.math.BigDecimal avgValue;
    }

}

