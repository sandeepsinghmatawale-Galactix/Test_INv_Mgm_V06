package com.barinventory.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.barinventory.config.LoginSuccessHandler;
import com.barinventory.config.SecurityUtils;
import com.barinventory.entities.UserBarAccess;
import com.barinventory.enums.GlobalRole;
import com.barinventory.repos.UserBarAccessRepository;
import com.barinventory.services.CustomUserDetails;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BarContextController {

    private final UserBarAccessRepository userBarAccessRepository;

    @GetMapping("/dashboard")
    public String dashboard() {
        Long barId = SecurityUtils.getActiveBarIdFromSession();
        if (barId == null) {
            return "redirect:/select-bar";
        }
        return "redirect:/sessions/create-page";
    }

    @GetMapping("/select-bar")
    public String selectBarPage(Model model) {
        CustomUserDetails principal = SecurityUtils.getCurrentUser();
        if (principal.getGlobalRole() == GlobalRole.ADMIN) {
            return "redirect:/admin/dashboard";
        }

        List<UserBarAccess> accesses = userBarAccessRepository
                .findActiveWithBarByUserId(principal.getUserId());
        model.addAttribute("accesses", accesses);
        return "auth/select-bar";
    }

    @PostMapping("/select-bar")
    public String selectBar(@RequestParam("barId") Long barId, HttpSession session) {
        CustomUserDetails principal = SecurityUtils.getCurrentUser();
        List<UserBarAccess> accesses = userBarAccessRepository
                .findActiveWithBarByUserId(principal.getUserId());

        boolean allowed = accesses.stream()
                .anyMatch(a -> a.getBar() != null && barId.equals(a.getBar().getBarId()));
        if (!allowed) {
            return "redirect:/no-access";
        }

        session.setAttribute(LoginSuccessHandler.ACTIVE_BAR_ID_SESSION_KEY, barId);
        return "redirect:/dashboard";
    }

    @GetMapping("/no-access")
    public String noAccess() {
        return "auth/no-access";
    }
}

