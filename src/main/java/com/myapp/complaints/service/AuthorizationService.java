package com.myapp.complaints.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public boolean checkAccess(String realOwnerEmail){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        return currentUser.equals(realOwnerEmail);
    }

    private Authentication getAuth(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean hasRole(String role){
        return getAuth().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    public boolean IsReceptionist() {
        return hasRole("ROLE_RECEPTIONIST");
    }

    public boolean isCitizen() {
        return hasRole("ROLE_CITIZEN");
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isManager() {
        return hasRole("ROLE_MANAGER");
    }

}
