//package com.myapp.complaints.service;
//
//import com.myapp.complaints.DAO.AccountRepo;
//import com.myapp.complaints.DAO.RoleRepo;
//import com.myapp.complaints.entity.Account;
//import com.myapp.complaints.entity.Role;
//import lombok.AllArgsConstructor;
//import lombok.NoArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//
//@AllArgsConstructor
//@NoArgsConstructor
//@Service
//public class AccountService {
//
//    private AccountRepo accountRepo;
//    private RoleRepo roleRepo;
//    public Account getDeletedAccountPlaceholder() {
//        return accountRepo.findByUserName("Deleted User")
//                .orElseGet(() -> {
//                    Account deleted = new Account();
//                    deleted.setUserName("Deleted User");
//                    deleted.setEmail("deleted@system.local");
//                    deleted.setPassword("0000");
//                    Role adminRole = roleRepo.findByName("مواطن");
//
//                    deleted.setRole(adminRole);
//                    return accountRepo.save(deleted);
//                });
//    }
//}
//
