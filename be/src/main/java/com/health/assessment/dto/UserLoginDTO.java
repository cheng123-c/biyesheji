package com.health.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginDTO {

    /**
     * 用户名或邮箱
     */
    @NotBlank(message = "用户名或邮箱不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

}

