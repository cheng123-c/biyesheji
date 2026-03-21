package com.health.assessment.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 认证过滤器
 *
 * 作用：
 * - 从请求头提取 JWT Token
 * - 验证 Token 有效性
 * - 在请求属性中存储用户信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

//    @Resource
    private final JwtTokenProvider jwtTokenProvider;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 从请求头中获取 Token
            String token = extractTokenFromRequest(request);

            // 如果存在 Token，进行验证
            if (token != null && !token.isEmpty()) {
                // 验证 Token
                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn("Token 验证失败");
                    sendUnauthorizedError(response, "Token 无效或已过期");
                    return;
                }

                // 从 Token 中提取用户信息
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                String username = jwtTokenProvider.getUsernameFromToken(token);

                // 将用户信息存储在请求属性中
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);

                // 创建 Spring Security 认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

                // 将认证对象设置到 SecurityContext 中
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Token 验证成功: userId={}, username={}", userId, username);
            }

        } catch (Exception ex) {
            log.error("认证过滤器异常: {}", ex.getMessage());
            sendUnauthorizedError(response, ex.getMessage());
            return;
        }

        // 继续过滤链
        filterChain.doFilter(request, response);
    }

    /**
     * 发送未授权错误响应
     */
    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", message);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }

    /**
     * 从请求头中提取 Token
     *
     * @param request HTTP 请求
     * @return Token 字符串，如果不存在返回 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

}

