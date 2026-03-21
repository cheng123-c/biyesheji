package com.health.assessment.mapper;

import com.health.assessment.entity.Permission;
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
 * 权限 Mapper 接口
 */
@Mapper
public interface PermissionMapper {

    /**
     * 根据ID查询权限
     */
    @Select("SELECT * FROM t_permission WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "permission_code", property = "permissionCode"),
            @Result(column = "permission_name", property = "permissionName"),
            @Result(column = "resource_type", property = "resourceType"),
            @Result(column = "operation", property = "operation"),
            @Result(column = "description", property = "description"),
            @Result(column = "created_at", property = "createdAt")
    })
    Permission selectById(Long id);

    /**
     * 根据权限代码查询权限
     */
    @Select("SELECT * FROM t_permission WHERE permission_code = #{permissionCode}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "permission_code", property = "permissionCode"),
            @Result(column = "permission_name", property = "permissionName"),
            @Result(column = "resource_type", property = "resourceType"),
            @Result(column = "operation", property = "operation"),
            @Result(column = "description", property = "description"),
            @Result(column = "created_at", property = "createdAt")
    })
    Permission selectByCode(String permissionCode);

    /**
     * 查询所有权限
     */
    @Select("SELECT * FROM t_permission")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "permission_code", property = "permissionCode"),
            @Result(column = "permission_name", property = "permissionName"),
            @Result(column = "resource_type", property = "resourceType"),
            @Result(column = "operation", property = "operation"),
            @Result(column = "description", property = "description"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Permission> selectAll();

    /**
     * 查询角色的所有权限
     */
    @Select("SELECT p.* FROM t_permission p " +
            "INNER JOIN t_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "permission_code", property = "permissionCode"),
            @Result(column = "permission_name", property = "permissionName"),
            @Result(column = "resource_type", property = "resourceType"),
            @Result(column = "operation", property = "operation"),
            @Result(column = "description", property = "description"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Permission> selectByRoleId(Long roleId);

    /**
     * 查询用户的所有权限
     */
    @Select("SELECT DISTINCT p.* FROM t_permission p " +
            "INNER JOIN t_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN t_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "permission_code", property = "permissionCode"),
            @Result(column = "permission_name", property = "permissionName"),
            @Result(column = "resource_type", property = "resourceType"),
            @Result(column = "operation", property = "operation"),
            @Result(column = "description", property = "description"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Permission> selectByUserId(Long userId);

    /**
     * 查询指定资源类型的权限
     */
    @Select("SELECT * FROM t_permission WHERE resource_type = #{resourceType}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "permission_code", property = "permissionCode"),
            @Result(column = "permission_name", property = "permissionName"),
            @Result(column = "resource_type", property = "resourceType"),
            @Result(column = "operation", property = "operation"),
            @Result(column = "description", property = "description"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Permission> selectByResourceType(String resourceType);

    /**
     * 新增权限
     */
    @Insert("INSERT INTO t_permission(permission_code, permission_name, resource_type, operation, description) " +
            "VALUES(#{permissionCode}, #{permissionName}, #{resourceType}, #{operation}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Permission permission);

    /**
     * 更新权限
     */
    @Update("UPDATE t_permission SET permission_name = #{permissionName}, " +
            "resource_type = #{resourceType}, operation = #{operation}, " +
            "description = #{description} WHERE id = #{id}")
    int update(Permission permission);

    /**
     * 删除权限
     */
    @Delete("DELETE FROM t_permission WHERE id = #{id}")
    int delete(Long id);

    /**
     * 批量删除权限
     */
    @Delete("<script>" +
            "DELETE FROM t_permission WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("ids") List<Long> ids);

}

