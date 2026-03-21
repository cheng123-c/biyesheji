package com.health.assessment.service;

import com.health.assessment.entity.User;
import com.health.assessment.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务类
 *
 * 说明：
 * - 全面使用 MyBatis 进行数据访问
 * - 没有 JPA 依赖
 * - 支持分页查询
 * - 支持事务管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 根据ID查询用户
     */
    public User getUserById(Long userId) {
        log.debug("查询用户: {}", userId);
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: {}", userId);
        }
        return user;
    }

    /**
     * 根据用户名查询用户
     */
    public User getUserByUsername(String username) {
        log.debug("根据用户名查询用户: {}", username);
        return userMapper.selectByUsername(username);
    }

    /**
     * 根据邮箱查询用户
     */
    public User getUserByEmail(String email) {
        log.debug("根据邮箱查询用户: {}", email);
        return userMapper.selectByEmail(email);
    }

    /**
     * 根据手机号查询用户
     */
    public User getUserByPhone(String phone) {
        log.debug("根据手机号查询用户: {}", phone);
        return userMapper.selectByPhone(phone);
    }

    /**
     * 分页查询所有活跃用户
     */
    public PageInfo<User> getActiveUsersPaginated(int pageNum, int pageSize) {
        log.debug("分页查询活跃用户，页码: {}，页大小: {}", pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userMapper.selectAllActiveUsers();
        return new PageInfo<>(users);
    }

    /**
     * 创建新用户
     */
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        log.info("创建新用户: {}", user.getUsername());

        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(user.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null) {
            existingUser = userMapper.selectByEmail(user.getEmail());
            if (existingUser != null) {
                throw new RuntimeException("邮箱已存在: " + user.getEmail());
            }
        }

        // 设置默认值
        user.setStatus("ACTIVE");
        user.setIsDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 插入数据库
        int result = userMapper.insert(user);
        if (result > 0) {
            log.info("用户创建成功，ID: {}", user.getId());
            return user;
        } else {
            throw new RuntimeException("创建用户失败");
        }
    }

    /**
     * 更新用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(User user) {
        log.info("更新用户: {}", user.getId());

        // 检查用户是否存在
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在: " + user.getId());
        }

        // 更新时间
        user.setUpdatedAt(LocalDateTime.now());

        // 执行更新
        int result = userMapper.update(user);
        if (result > 0) {
            log.info("用户更新成功: {}", user.getId());
            return user;
        } else {
            throw new RuntimeException("更新用户失败");
        }
    }

    /**
     * 更新用户密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserPassword(Long userId, String newPasswordHash) {
        log.info("更新用户密码: {}", userId);

        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        // 执行密码更新
        int result = userMapper.updatePassword(userId, newPasswordHash);
        if (result > 0) {
            log.info("用户密码更新成功: {}", userId);
        } else {
            throw new RuntimeException("更新密码失败");
        }
    }

    /**
     * 更新用户状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, String status) {
        log.info("更新用户状态: {} -> {}", userId, status);

        // 验证状态值
        if (!status.matches("ACTIVE|INACTIVE|BANNED")) {
            throw new RuntimeException("无效的状态值: " + status);
        }

        int result = userMapper.updateStatus(userId, status);
        if (result > 0) {
            log.info("用户状态更新成功: {} -> {}", userId, status);
        } else {
            throw new RuntimeException("更新状态失败");
        }
    }

    /**
     * 软删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        log.info("删除用户: {}", userId);

        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        // 执行软删除
        int result = userMapper.softDelete(userId);
        if (result > 0) {
            log.info("用户删除成功: {}", userId);
        } else {
            throw new RuntimeException("删除用户失败");
        }
    }

    /**
     * 硬删除用户（谨慎使用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void permanentlyDeleteUser(Long userId) {
        log.warn("执行硬删除用户: {}", userId);

        int result = userMapper.delete(userId);
        if (result > 0) {
            log.warn("用户已永久删除: {}", userId);
        } else {
            throw new RuntimeException("删除用户失败");
        }
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean usernameExists(String username) {
        User user = userMapper.selectByUsername(username);
        return user != null;
    }

    /**
     * 检查邮箱是否已存在
     */
    public boolean emailExists(String email) {
        User user = userMapper.selectByEmail(email);
        return user != null;
    }

}

