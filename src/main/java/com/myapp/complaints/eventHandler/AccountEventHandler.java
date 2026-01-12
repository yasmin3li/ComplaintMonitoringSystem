package com.myapp.complaints.eventHandler;

import com.myapp.complaints.DAO.*;
import com.myapp.complaints.entity.Role;
import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.entity.Account;
//import com.myapp.complaints.service.AccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Transactional
@Slf4j
@Component
@RequiredArgsConstructor
//@AllArgsConstructor
//@NoArgsConstructor
@RepositoryEventHandler(Account.class)
public class AccountEventHandler {


    private final PasswordEncoder passwordEncoder;
    private final RoleRepo roleRepository;

    /**
     * Soft delete: mark the account as deleted and deactivate it
     */
    @HandleBeforeDelete
    public void handleDelete(Account account) {
        account.setDeleted(true);
        account.setStatus(AccountStatus.DEACTIVATED);
        throw new RuntimeException("Physical delete is not allowed. Account has been deactivated.");
    }

    /**
     * Handle password update: re-encode if changed and reset mustChangePassword flag
     */
    @HandleBeforeSave
    public void handleUpdate(Account account) {
        if (!account.getPassword().startsWith("$2a$")) {
            account.setPassword(validateAndEncodePassword(account.getPassword()));
            account.setMustChangePassword(false);
            account.setStatus(AccountStatus.ACTIVATED);
        }
    }

    /**
     * Handle account creation: assign defaults
     */
    @HandleBeforeCreate
    public void handleAccountCreate(Account account) {

        if (account.getRole() == null) {
            Role citizenRole = roleRepository.findByName("مواطن")
                    .orElseThrow(() -> new IllegalStateException("Default role 'مواطن' not found"));
            account.setRole(citizenRole);
        }

        // Temporary email if missing
        if (account.getEmail() == null || account.getEmail().isBlank()) {
            String phone = account.getPhoneNumber() != null ? account.getPhoneNumber() : "user" + System.currentTimeMillis();
            account.setEmail(phone + "@example.com");
            account.setEmailTemporary(true);
        } else {
            account.setEmailTemporary(false);
        }

        // Encode password
        account.setPassword(validateAndEncodePassword(account.getPassword()));

        //  Default status = BANNED
        account.setStatus(AccountStatus.BANNED);
        account.setDeleted(false);

        // Must change password logic
        String roleName = account.getRole().getName();
        // Citizen or Admin does not need to
        account.setMustChangePassword(roleName.equals("موظف الاستقبال") || roleName.equals("مدير")); // Employee and Manager must change password
    }
    private  String validateAndEncodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!rawPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
//        if (!rawPassword.matches(".*[a-z].*")) {
//            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
//        }
        if (!rawPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!rawPassword.matches(".*[!@#$%^&*].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
//        if (!rawPassword.matches(".*[]\\[!@#$%^&*()_+\\-={};':\"\\\\|,.<>/?].*")) {
//            throw new IllegalArgumentException("Password must contain at least one special character");
//        }
        // تشفير كلمة المرور
        return passwordEncoder.encode(rawPassword);
    }

}

//    @HandleBeforeCreate
//    public void handleAccountCreate(Account account) {
//        if (account.getRole() == null) {
//            Role defaultRole = roleRepository.findByName("مواطن");
//            account.setRole(defaultRole);
//        }
//    }
//
//    @HandleBeforeDelete
//    public void handleDelete(Account account) {
//
//
//        // Nullify كل الشكاوى المرتبطة بالحساب
//        List<Complaint> complaints = complaintRepo.findByAddedBy(account);
//        for (Complaint c : complaints) {
//            c.setAddedBy(null);
//        }

// add delete logic for rating/voting/complaint
//        Account deletedAccount = accountService.getDeletedAccountPlaceholder();
//
//        // إعادة تعيين account في Ratings
//        ratingRepository.findAllByAccount(account).forEach(r -> {
//            r.setAccount(deletedAccount);
//            ratingRepository.save(r);
//        });
//
//        // إعادة تعيين account في Votings
//        votingRepository.findAllByAccount(account).forEach(v -> {
//            v.setAccount(deletedAccount);
//            votingRepository.save(v);
//        });
//
//        // إذا كانت هناك شكاوى مرتبطة بالحساب
//        complaintRepo.findAllByAddedBy(account).forEach(c -> {
//            c.setAddedBy(deletedAccount);
//            complaintRepo.save(c);
//        });

//}
