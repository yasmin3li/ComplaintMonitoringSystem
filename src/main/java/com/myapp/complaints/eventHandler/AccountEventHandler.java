package com.myapp.complaints.handler;

import com.myapp.complaints.DAO.RoleRepo;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Transactional
@Slf4j
@Component
@RequiredArgsConstructor
@RepositoryEventHandler(Account.class)
public class AccountEventHandler {

    private final RoleRepo roleRepository;

    @HandleBeforeCreate
    public void handleAccountCreate(Account account) {
        if (account.getRole() == null) {
            Role defaultRole = roleRepository.findByName("مواطن")
//                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            account.setRole(defaultRole);
        }
    }
}
