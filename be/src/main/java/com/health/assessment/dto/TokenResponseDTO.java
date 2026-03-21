package com.health.assessment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token 响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponseDTO {

    /**
     * 访问 Token
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 刷新 Token
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * Token 类型
     */
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 过期时间（秒）
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Long userId;

    /**
     * 用户名
     */
    private String username;

}

