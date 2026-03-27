package com.health.assessment.controller;

import com.github.pagehelper.PageInfo;
import com.health.assessment.common.ApiResponse;
import com.health.assessment.dto.UserProfileDTO;
import com.health.assessment.entity.User;
import com.health.assessment.exception.AuthenticationException;
import com.health.assessment.exception.BusinessException;
import com.health.assessment.security.JwtTokenProvider;
import com.health.assessment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户控制器
 *
 * 处理用户信息相关的请求
 */
@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息相关接口")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 获取当前用户信息
     *
     * @param request HTTP 请求
     * @return 当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取登录用户的详细信息")
    public ApiResponse<UserProfileDTO> getCurrentUser(HttpServletRequest request) {
        log.info("获取当前用户信息");

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new AuthenticationException("未能获取用户ID，请重新登录");
        }

        User user = userService.getUserById(userId);
        UserProfileDTO profileDTO = convertToProfileDTO(user);
        return ApiResponse.success(profileDTO);
    }

    /**
     * 获取用户信息（根据ID）
     * 只允许查看自己的信息，防止越权访问
     *
     * @param request HTTP 请求
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息（只能查看自己）")
    public ApiResponse<UserProfileDTO> getUserById(
            HttpServletRequest request,
            @PathVariable Long userId) {
        log.info("获取用户信息: userId={}", userId);

        // 防止越权：只允许用户查看自己的信息
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            throw new AuthenticationException("未能获取用户ID，请重新登录");
        }
        if (!currentUserId.equals(userId)) {
            throw new BusinessException("无权限查看其他用户信息");
        }

        User user = userService.getUserById(userId);
        UserProfileDTO profileDTO = convertToProfileDTO(user);

        return ApiResponse.success(profileDTO);
    }

    /**
     * 更新用户信息
     *
     * @param request HTTP 请求
     * @param userProfileDTO 用户信息
     * @return 更新结果
     */
    @PutMapping("/me")
    @Operation(summary = "更新用户信息", description = "更新当前用户的个人信息")
    public ApiResponse<UserProfileDTO> updateCurrentUser(
            HttpServletRequest request,
            @Valid @RequestBody UserProfileDTO userProfileDTO) {
        log.info("更新用户信息");

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new AuthenticationException("未能获取用户ID，请重新登录");
        }

        // 构建更新用户对象
        User user = User.builder()
                .id(userId)
                .realName(userProfileDTO.getRealName())
                .age(userProfileDTO.getAge())
                .gender(userProfileDTO.getGender())
                .avatarUrl(userProfileDTO.getAvatarUrl())
                .bio(userProfileDTO.getBio())
                .build();

        User updatedUser = userService.updateUser(user);
        UserProfileDTO profileDTO = convertToProfileDTO(updatedUser);

        return ApiResponse.success("用户信息更新成功", profileDTO);
    }

    /**
     * 删除用户账户
     *
     * @param request HTTP 请求
     * @return 删除结果
     */
    @DeleteMapping("/me")
    @Operation(summary = "删除用户账户", description = "删除当前用户的账户")
    public ApiResponse<String> deleteCurrentUser(HttpServletRequest request) {
        log.info("删除用户账户");

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new AuthenticationException("未能获取用户ID，请重新登录");
        }

        userService.deleteUser(userId);

        return ApiResponse.success("账户已删除", "");
    }

    /**
     * 获取用户列表（分页）
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户列表
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "分页获取活跃用户列表")
    public ApiResponse<PageInfo<UserProfileDTO>> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("获取用户列表: pageNum={}, pageSize={}", pageNum, pageSize);

        PageInfo<User> pageInfo = userService.getActiveUsersPaginated(pageNum, pageSize);

        // 转换为 DTO
        PageInfo<UserProfileDTO> dtoPageInfo = new PageInfo<>();
        dtoPageInfo.setPageNum(pageInfo.getPageNum());
        dtoPageInfo.setPageSize(pageInfo.getPageSize());
        dtoPageInfo.setTotal(pageInfo.getTotal());
        dtoPageInfo.setPages(pageInfo.getPages());
        dtoPageInfo.setList(pageInfo.getList().stream()
                .map(this::convertToProfileDTO)
                .collect(Collectors.toList()));

        return ApiResponse.success(dtoPageInfo);
    }

    /**
     * 将 User 转换为 UserProfileDTO
     *
     * @param user 用户对象
     * @return 用户信息 DTO
     */
    private UserProfileDTO convertToProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .realName(user.getRealName())
                .age(user.getAge())
                .gender(user.getGender())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .status(user.getStatus())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}

