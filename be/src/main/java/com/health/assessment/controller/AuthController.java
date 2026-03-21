package com.health.assessment.controller;

import com.health.assessment.common.ApiResponse;
import com.health.assessment.dto.PasswordChangeDTO;
import com.health.assessment.dto.TokenResponseDTO;
import com.health.assessment.dto.UserLoginDTO;
import com.health.assessment.dto.UserRegisterDTO;
import com.health.assessment.entity.User;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 *
 * 处理用户认证相关的请求
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账户")
    public ApiResponse<User> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        log.info("用户注册请求: username={}", registerDTO.getUsername());
        User newUser = authService.register(registerDTO);
        return ApiResponse.success("注册成功", newUser);
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return Token 信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名/邮箱和密码登录")
    public ApiResponse<TokenResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        log.info("用户登录请求: username={}", loginDTO.getUsername());
        TokenResponseDTO tokenResponse = authService.login(loginDTO);
        return ApiResponse.success("登录成功", tokenResponse);
    }

    /**
     * 刷新 Token
     *
     * @param refreshToken 刷新 Token
     * @return 新的 Token 信息
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用刷新 Token 获取新的访问 Token")
    public ApiResponse<TokenResponseDTO> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String refreshToken) {
        log.info("刷新 Token 请求");

        // 提取 Bearer token
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        TokenResponseDTO tokenResponse = authService.refreshToken(refreshToken);
        return ApiResponse.success("Token 刷新成功", tokenResponse);
    }

    /**
     * 修改密码
     *
     * @param request HTTP 请求
     * @param passwordChangeDTO 密码修改信息
     * @return 修改结果
     */
    @PutMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前用户的密码")
    public ApiResponse<String> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        log.info("修改密码请求");

        // 从请求属性中获取用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new AuthenticationException("未能获取用户ID，请重新登录");
        }

        authService.changePassword(userId, passwordChangeDTO);
        return ApiResponse.success("密码修改成功", "");
    }

}

