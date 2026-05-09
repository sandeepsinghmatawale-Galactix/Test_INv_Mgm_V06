package com.barinventory.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.barinventory.services.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SecurityUtils {

    public static CustomUserDetails getCurrentUser() {

        return (CustomUserDetails)
                SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public static Long getBarId() {
        Long sessionBarId = getActiveBarIdFromSession();
        if (sessionBarId != null) {
            return sessionBarId;
        }
        return getCurrentUser().getBarId();
    }

    public static String getUsername() {
        return getCurrentUser().getUsername();
    }

    public static Long getActiveBarIdFromSession() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
            return null;
        }
        HttpServletRequest request = servletAttrs.getRequest();
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(LoginSuccessHandler.ACTIVE_BAR_ID_SESSION_KEY);
        if (value instanceof Long id) {
            return id;
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
