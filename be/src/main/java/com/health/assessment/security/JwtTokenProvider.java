package com.health.assessment.security;

import com.health.assessment.exception.AuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

/**
 * JWT Token 工具类
 *
 * 功能：
 * - 生成 Access Token
 * - 生成 Refresh Token
 * - 验证 Token
 * - 提取 Token 中的信息
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * 生成 Access Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return Access Token
     */
    public String generateAccessToken(Long userId, String username) {
        log.debug("生成 Access Token: userId={}, username={}", userId, username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "access");

        return createToken(claims, jwtExpiration);
    }

    /**
     * 生成 Refresh Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId, String username) {
        log.debug("生成 Refresh Token: userId={}, username={}", userId, username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");

        return createToken(claims, refreshExpiration);
    }

    /**
     * 创建 Token
     *
     * @param claims Token 声明
     * @param expirationTime 过期时间（毫秒）
     * @return Token
     */
    private String createToken(Map<String, Object> claims, Long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 验证 Token
     *
     * @param token Token 字符串
     * @return true 如果 Token 有效
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            log.warn("Token 验证失败: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * 从 Token 中获取 Claims
     *
     * @param token Token 字符串
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ex) {
            log.error("解析 Token 失败: {}", ex.getMessage());
            throw new AuthenticationException("无效的 Token");
        }
    }

    /**
     * 从 Token 中获取 User ID
     *
     * @param token Token 字符串
     * @return User ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        throw new AuthenticationException("Token 中找不到用户ID");
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token Token 字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object username = claims.get("username");
        if (username instanceof String) {
            return (String) username;
        }
        throw new AuthenticationException("Token 中找不到用户名");
    }

    /**
     * 从 Token 中获取过期时间
     *
     * @param token Token 字符串
     * @return 过期时间（毫秒）
     */
    public long getExpirationTimeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Date expiration = claims.getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * 检查 Token 是否过期
     *
     * @param token Token 字符串
     * @return true 如果已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception ex) {
            return true;
        }
    }

}

