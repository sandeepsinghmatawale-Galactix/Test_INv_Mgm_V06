package com.barinventory.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.barinventory.entities.BarUser;
import com.barinventory.enums.GlobalRole;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		BarUser user = (BarUser) authentication.getPrincipal();

		// ADMIN
		if (user.getRole() == GlobalRole.ADMIN) {

			response.sendRedirect("/admin/dashboard");

			return;
		}

		// NON ADMIN USERS

		int totalBars = user.getActiveBarAccesses().size();

		// NO BAR ACCESS
		if (totalBars == 0) {

			response.sendRedirect("/no-access");

			return;
		}

		// SINGLE BAR
		if (totalBars == 1) {

			Long barId = user.getActiveBarAccesses().get(0).getBar().getBarId();

			request.getSession().setAttribute("ACTIVE_BAR_ID", barId);

			response.sendRedirect("/dashboard");

			return;
		}

		// MULTIPLE BARS

		response.sendRedirect("/select-bar");
	}
}