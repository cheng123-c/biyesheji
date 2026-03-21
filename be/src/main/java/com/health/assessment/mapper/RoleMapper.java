package com.health.assessment.mapper;

import com.health.assessment.entity.Role;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 角色 Mapper 接口
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据ID查询角色
     */
    @Select("SELECT * FROM t_role WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "role_name", property = "roleName"),
            @Result(column = "description", property = "description"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt")
    })
    Role selectById(Long id);

    /**
     * 根据角色名查询角色
     */
    @Select("SELECT * FROM t_role WHERE role_name = #{roleName}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "role_name", property = "roleName"),
            @Result(column = "description", property = "description"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt")
    })
    Role selectByName(String roleName);

    /**
     * 查询所有活跃角色
     */
    @Select("SELECT * FROM t_role WHERE status = 'ACTIVE'")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "role_name", property = "roleName"),
            @Result(column = "description", property = "description"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Role> selectAllActive();

    /**
     * 查询所有角色
     */
    @Select("SELECT * FROM t_role")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "role_name", property = "roleName"),
            @Result(column = "description", property = "description"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Role> selectAll();

    /**
     * 新增角色
     */
    @Insert("INSERT INTO t_role(role_name, description, status) " +
            "VALUES(#{roleName}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Role role);

    /**
     * 更新角色
     */
    @Update("UPDATE t_role SET role_name = #{roleName}, description = #{description}, " +
            "status = #{status} WHERE id = #{id}")
    int update(Role role);

    /**
     * 删除角色
     */
    @Delete("DELETE FROM t_role WHERE id = #{id}")
    int delete(Long id);

}

