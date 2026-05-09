package com.barinventory.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.barinventory.entities.UserBarAccess;
import com.barinventory.enums.GlobalRole;
import com.barinventory.repos.UserBarAccessRepository;
import com.barinventory.services.CustomUserDetails;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    public static final String ACTIVE_BAR_ID_SESSION_KEY = "ACTIVE_BAR_ID";

    private final UserBarAccessRepository userBarAccessRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

		// ADMIN
		if (principal.getGlobalRole() == GlobalRole.ADMIN) {

			response.sendRedirect("/admin/dashboard");

			return;
		}

		// NON ADMIN USERS

        List<UserBarAccess> accesses = userBarAccessRepository
                .findActiveWithBarByUserId(principal.getUserId());

		int totalBars = accesses.size();

		// NO BAR ACCESS
		if (totalBars == 0) {

			response.sendRedirect("/no-access");

			return;
		}

		// SINGLE BAR
		if (totalBars == 1) {

			Long barId = accesses.get(0).getBar().getBarId();

			request.getSession().setAttribute(ACTIVE_BAR_ID_SESSION_KEY, barId);

			response.sendRedirect("/dashboard");

			return;
		}

		// MULTIPLE BARS

		response.sendRedirect("/select-bar");
	}
}
