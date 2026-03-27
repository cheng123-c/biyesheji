package com.health.assessment.service;

import com.health.assessment.dto.PasswordChangeDTO;
import com.health.assessment.dto.TokenResponseDTO;
import com.health.assessment.dto.UserLoginDTO;
import com.health.assessment.dto.UserRegisterDTO;
import com.health.assessment.entity.User;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.exception.ResourceNotFoundException;
import com.health.assessment.mapper.UserMapper;
import com.health.assessment.security.JwtTokenProvider;
import com.health.assessment.security.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务
 *
 * 功能：
 * - 用户注册
 * - 用户登录
 * - Token 刷新
 * - 密码修改
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 新创建的用户
     */
    @Transactional(rollbackFor = Exception.class)
    public User register(UserRegisterDTO registerDTO) {
        log.info("用户注册请求: username={}", registerDTO.getUsername());

        // 验证两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        existingUser = userMapper.selectByEmail(registerDTO.getEmail());
        if (existingUser != null) {
            throw new BusinessException("邮箱已被注册");
        }

        // 检查手机号是否已存在（如果提供且非空）
        if (registerDTO.getPhone() != null && !registerDTO.getPhone().trim().isEmpty()) {
            existingUser = userMapper.selectByPhone(registerDTO.getPhone().trim());
            if (existingUser != null) {
                throw new BusinessException("手机号已被注册");
            }
        }

        // 创建新用户（phone/realName 为空字符串时存为 null，避免唯一索引冲突）
        String phone = (registerDTO.getPhone() != null && !registerDTO.getPhone().trim().isEmpty())
                ? registerDTO.getPhone().trim() : null;
        String realName = (registerDTO.getRealName() != null && !registerDTO.getRealName().trim().isEmpty())
                ? registerDTO.getRealName().trim() : null;
        User newUser = User.builder()
                .username(registerDTO.getUsername())
                .email(registerDTO.getEmail())
                .phone(phone)
                .realName(realName)
                .passwordHash(passwordEncoder.encode(registerDTO.getPassword()))
                .status("ACTIVE")
                .role("USER")  // 默认角色为普通用户
                .gender("UNKNOWN")
                .isDeleted(0) // 0 = 未删除, 1 = 已删除
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 保存到数据库
        int result = userMapper.insert(newUser);
        if (result > 0) {
            log.info("用户注册成功: userId={}, username={}", newUser.getId(), newUser.getUsername());
            return newUser;
        } else {
            throw new BusinessException("用户注册失败，请稍后重试");
        }
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return Token 响应
     */
    public TokenResponseDTO login(UserLoginDTO loginDTO) {
        log.info("用户登录请求: username={}", loginDTO.getUsername());

        // 查询用户（支持用户名或邮箱登录）
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            user = userMapper.selectByEmail(loginDTO.getUsername());
        }

        // 用户不存在
        if (user == null) {
            log.warn("登录失败: 用户不存在 - {}", loginDTO.getUsername());
            throw new AuthenticationException("用户名或密码错误");
        }

        // 用户被禁用
        if ("BANNED".equals(user.getStatus())) {
            log.warn("登录失败: 用户被禁用 - userId={}", user.getId());
            throw new AuthenticationException("账户已被禁用");
        }

        // 用户已删除
        if (user.getIsDeleted() != null && user.getIsDeleted() == 1) {
            log.warn("登录失败: 用户已删除 - userId={}", user.getId());
            throw new AuthenticationException("账户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            log.warn("登录失败: 密码错误 - userId={}", user.getId());
            throw new AuthenticationException("用户名或密码错误");
        }

        // 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)  // 24 小时
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    /**
     * 刷新 Token
     *
     * @param refreshToken 刷新 Token
     * @return 新的 Token 响应
     */
    public TokenResponseDTO refreshToken(String refreshToken) {
        log.debug("刷新 Token 请求");

        // 验证刷新 Token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("刷新 Token 失败: Token 无效或已过期");
            throw new AuthenticationException("刷新 Token 无效或已过期");
        }

        // 提取用户信息
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        // 生成新的 Token
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, username);

        log.info("Token 刷新成功: userId={}", userId);

        return TokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(userId)
                .username(username)
                .build();
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param passwordChangeDTO 密码修改信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, PasswordChangeDTO passwordChangeDTO) {
        log.info("修改密码请求: userId={}", userId);

        // 验证两次新密码是否一致
        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmNewPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(passwordChangeDTO.getOldPassword(), user.getPasswordHash())) {
            log.warn("修改密码失败: 旧密码错误 - userId={}", userId);
            throw new BusinessException("旧密码错误");
        }

        // 更新密码
        String newPasswordHash = passwordEncoder.encode(passwordChangeDTO.getNewPassword());
        int result = userMapper.updatePassword(userId, newPasswordHash);

        if (result > 0) {
            log.info("密码修改成功: userId={}", userId);
        } else {
            throw new BusinessException("密码修改失败，请稍后重试");
        }
    }

    /**
     * 验证 Token 有效性
     *
     * @param token Token 字符串
     * @return true 如果有效
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 从 Token 获取用户ID
     *
     * @param token Token 字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }

}

