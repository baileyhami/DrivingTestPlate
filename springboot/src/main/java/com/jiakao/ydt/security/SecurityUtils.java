package com.jiakao.ydt.security;

import com.jiakao.ydt.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static SecurityUser requireUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser u)) {
            throw new BusinessException(401, "未登录");
        }
        return u;
    }

    public static boolean hasRole(String role) {
        try {
            return requireUser().getRoleCode().equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }
}
