package com.health.assessment.mapper;

import com.health.assessment.entity.User;
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
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     */
    @Select("SELECT * FROM t_user WHERE id = #{id} AND is_deleted = 0")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "username", property = "username"),
            @Result(column = "phone", property = "phone"),
            @Result(column = "email", property = "email"),
            @Result(column = "password_hash", property = "passwordHash"),
            @Result(column = "real_name", property = "realName"),
            @Result(column = "age", property = "age"),
            @Result(column = "gender", property = "gender"),
            @Result(column = "avatar_url", property = "avatarUrl"),
            @Result(column = "bio", property = "bio"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "deleted_at", property = "deletedAt"),
            @Result(column = "is_deleted", property = "isDeleted")
    })
    User selectById(Long id);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM t_user WHERE username = #{username} AND is_deleted = 0")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "username", property = "username"),
            @Result(column = "phone", property = "phone"),
            @Result(column = "email", property = "email"),
            @Result(column = "password_hash", property = "passwordHash"),
            @Result(column = "real_name", property = "realName"),
            @Result(column = "age", property = "age"),
            @Result(column = "gender", property = "gender"),
            @Result(column = "avatar_url", property = "avatarUrl"),
            @Result(column = "bio", property = "bio"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "deleted_at", property = "deletedAt"),
            @Result(column = "is_deleted", property = "isDeleted")
    })
    User selectByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM t_user WHERE email = #{email} AND is_deleted = 0")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "username", property = "username"),
            @Result(column = "phone", property = "phone"),
            @Result(column = "email", property = "email"),
            @Result(column = "password_hash", property = "passwordHash"),
            @Result(column = "real_name", property = "realName"),
            @Result(column = "age", property = "age"),
            @Result(column = "gender", property = "gender"),
            @Result(column = "avatar_url", property = "avatarUrl"),
            @Result(column = "bio", property = "bio"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "deleted_at", property = "deletedAt"),
            @Result(column = "is_deleted", property = "isDeleted")
    })
    User selectByEmail(String email);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM t_user WHERE phone = #{phone} AND is_deleted = 0")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "username", property = "username"),
            @Result(column = "phone", property = "phone"),
            @Result(column = "email", property = "email"),
            @Result(column = "password_hash", property = "passwordHash"),
            @Result(column = "real_name", property = "realName"),
            @Result(column = "age", property = "age"),
            @Result(column = "gender", property = "gender"),
            @Result(column = "avatar_url", property = "avatarUrl"),
            @Result(column = "bio", property = "bio"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "deleted_at", property = "deletedAt"),
            @Result(column = "is_deleted", property = "isDeleted")
    })
    User selectByPhone(String phone);

    /**
     * 查询所有活跃用户
     */
    @Select("SELECT * FROM t_user WHERE status = 'ACTIVE' AND is_deleted = 0 ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "username", property = "username"),
            @Result(column = "phone", property = "phone"),
            @Result(column = "email", property = "email"),
            @Result(column = "password_hash", property = "passwordHash"),
            @Result(column = "real_name", property = "realName"),
            @Result(column = "age", property = "age"),
            @Result(column = "gender", property = "gender"),
            @Result(column = "avatar_url", property = "avatarUrl"),
            @Result(column = "bio", property = "bio"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "deleted_at", property = "deletedAt"),
            @Result(column = "is_deleted", property = "isDeleted")
    })
    List<User> selectAllActiveUsers();

    /**
     * 新增用户
     */
    @Insert("INSERT INTO t_user(username, email, phone, password_hash, real_name, gender, status) " +
            "VALUES(#{username}, #{email}, #{phone}, #{passwordHash}, #{realName}, #{gender}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 更新用户个人资料（仅允许更新 realName/age/gender/avatarUrl/bio，不允许修改 username/email/phone）
     */
    @Update("UPDATE t_user SET " +
            "real_name = #{realName}, age = #{age}, gender = #{gender}, avatar_url = #{avatarUrl}, " +
            "bio = #{bio}, updated_at = NOW() " +
            "WHERE id = #{id} AND is_deleted = 0")
    int update(User user);

    /**
     * 更新用户密码
     */
    @Update("UPDATE t_user SET password_hash = #{passwordHash}, updated_at = NOW() " +
            "WHERE id = #{id} AND is_deleted = 0")
    int updatePassword(Long id, String passwordHash);

    /**
     * 更新用户状态
     */
    @Update("UPDATE t_user SET status = #{status}, updated_at = NOW() " +
            "WHERE id = #{id} AND is_deleted = 0")
    int updateStatus(Long id, String status);

    /**
     * 软删除用户
     */
    @Update("UPDATE t_user SET is_deleted = 1, deleted_at = NOW(), updated_at = NOW() " +
            "WHERE id = #{id}")
    int softDelete(Long id);

    /**
     * 硬删除用户
     */
    @Delete("DELETE FROM t_user WHERE id = #{id}")
    int delete(Long id);

}

