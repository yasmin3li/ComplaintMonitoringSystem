package com.myapp.complaints.config.user;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class UserInfoConfig implements UserDetails {

    private final Account account;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays
                .stream(account
                        .getRole()
                        .getName()
                        .split(",")
                )
                .map(this::mapArabicRoleToAuthority)
                .toList();
    }

    private GrantedAuthority mapArabicRoleToAuthority(String role) {
        return switch (role.trim()) {
            case "مواطن" -> new SimpleGrantedAuthority("ROLE_USER");
            case "موظف الاستقبال" -> new SimpleGrantedAuthority("ROLE_RECEPTIONIST");
            case "مدير" -> new SimpleGrantedAuthority("ROLE_MANAGER");
            case "أدمن" -> new SimpleGrantedAuthority("ROLE_ADMIN");
//            case role -> new SimpleGrantedAuthority("ROLE_"+role);
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
            return  account.getStatus() == AccountStatus.ACTIVATED && !account.isDeleted();
        }

    }

//TODO:Checks account status (active / banned / deleted)
