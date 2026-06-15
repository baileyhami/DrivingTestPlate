package com.jiakao.ydt.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiakao.ydt.common.R;
import com.jiakao.ydt.common.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 从 Authorization: Bearer 解析 JWT 并写入 SecurityContext
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtUtils.parse(token);
                Long uid = Long.parseLong(claims.getSubject());
                String role = claims.get("role", String.class);
                Object sidObj = claims.get("schoolId");
                Long schoolId = sidObj == null ? null : ((Number) sidObj).longValue();
                SecurityUser principal = new SecurityUser();
                principal.setId(uid);
                principal.setUsername("");
                principal.setPassword("");
                principal.setRoleCode(role);
                principal.setSchoolId(schoolId);
                principal.setEnabled(true);
                var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException e) {
                writeJson(response, ResultCode.UNAUTHORIZED.getCode(), "登录已过期，请重新登录");
                return;
            } catch (JwtException | IllegalArgumentException e) {
                writeJson(response, ResultCode.UNAUTHORIZED.getCode(), "无效的令牌");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeJson(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(R.fail(code, message)));
    }
}
