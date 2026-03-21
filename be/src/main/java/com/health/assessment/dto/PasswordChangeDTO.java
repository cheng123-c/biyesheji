package com.health.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 密码修改 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordChangeDTO {

    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "新密码长度应为 6-50 字符")
    private String newPassword;

    /**
     * 确认新密码
     */
    @NotBlank(message = "确认新密码不能为空")
    private String confirmNewPassword;

}

