package com.health.assessment.mapper;

import com.health.assessment.entity.MedicalRecord;
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
 * 医疗记录 Mapper 接口
 */
@Mapper
public interface MedicalRecordMapper {

    /**
     * 根据ID查询医疗记录
     */
    @Select("SELECT * FROM t_medical_record WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "record_type", property = "recordType"),
            @Result(column = "record_title", property = "recordTitle"),
            @Result(column = "record_content", property = "recordContent"),
            @Result(column = "record_date", property = "recordDate"),
            @Result(column = "hospital", property = "hospital"),
            @Result(column = "doctor_name", property = "doctorName"),
            @Result(column = "created_at", property = "createdAt")
    })
    MedicalRecord selectById(Long id);

    /**
     * 查询用户的所有医疗记录（按日期降序）
     */
    @Select("SELECT * FROM t_medical_record WHERE user_id = #{userId} ORDER BY record_date DESC, created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "record_type", property = "recordType"),
            @Result(column = "record_title", property = "recordTitle"),
            @Result(column = "record_content", property = "recordContent"),
            @Result(column = "record_date", property = "recordDate"),
            @Result(column = "hospital", property = "hospital"),
            @Result(column = "doctor_name", property = "doctorName"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<MedicalRecord> selectByUserId(Long userId);

    /**
     * 根据类型查询用户医疗记录
     */
    @Select("SELECT * FROM t_medical_record WHERE user_id = #{userId} AND record_type = #{recordType} " +
            "ORDER BY record_date DESC, created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "record_type", property = "recordType"),
            @Result(column = "record_title", property = "recordTitle"),
            @Result(column = "record_content", property = "recordContent"),
            @Result(column = "record_date", property = "recordDate"),
            @Result(column = "hospital", property = "hospital"),
            @Result(column = "doctor_name", property = "doctorName"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<MedicalRecord> selectByUserIdAndType(@Param("userId") Long userId,
                                              @Param("recordType") String recordType);

    /**
     * 新增医疗记录
     */
    @Insert("INSERT INTO t_medical_record(user_id, record_type, record_title, record_content, " +
            "record_date, hospital, doctor_name) " +
            "VALUES(#{userId}, #{recordType}, #{recordTitle}, #{recordContent}, " +
            "#{recordDate}, #{hospital}, #{doctorName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MedicalRecord record);

    /**
     * 更新医疗记录
     */
    @Update("UPDATE t_medical_record SET record_type = #{recordType}, record_title = #{recordTitle}, " +
            "record_content = #{recordContent}, record_date = #{recordDate}, " +
            "hospital = #{hospital}, doctor_name = #{doctorName} " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int update(MedicalRecord record);

    /**
     * 删除医疗记录
     */
    @Delete("DELETE FROM t_medical_record WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 统计用户医疗记录数量
     */
    @Select("SELECT COUNT(*) FROM t_medical_record WHERE user_id = #{userId}")
    Integer countByUserId(Long userId);

    /**
     * 分页查询用户医疗记录（支持 recordType/keyword 过滤）
     * （定义在 MedicalRecordMapper.xml 中，使用 resultMap 映射字段）
     */
    List<MedicalRecord> selectByUserIdWithPage(@Param("userId") Long userId,
                                               @Param("recordType") String recordType,
                                               @Param("keyword") String keyword,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit);

    /**
     * 统计用户医疗记录数量（支持 recordType/keyword 过滤）
     * （定义在 MedicalRecordMapper.xml 中）
     */
    Integer countByUserIdWithCondition(@Param("userId") Long userId,
                                       @Param("recordType") String recordType,
                                       @Param("keyword") String keyword);

    /**
     * 按类型统计医疗记录数量
     * （定义在 MedicalRecordMapper.xml 中）
     */
    List<java.util.Map<String, Object>> countByType(@Param("userId") Long userId);

    /**
     * 查询最近的医疗记录
     * （定义在 MedicalRecordMapper.xml 中，使用 resultMap 映射字段）
     */
    List<MedicalRecord> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}

