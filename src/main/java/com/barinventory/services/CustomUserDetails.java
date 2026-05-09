package com.barinventory.services;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.barinventory.entities.BarUser;
import com.barinventory.enums.GlobalRole;

public class CustomUserDetails implements UserDetails {

    private final BarUser user;

    public CustomUserDetails(BarUser user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getId();
    }

    public GlobalRole getGlobalRole() {
        return user.getRole();
    }

    public Long getBarId() {
        return user.getBarId();
    }

    public BarUser getUser() {
        return user;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        GlobalRole role = user.getRole();
        if (role == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
