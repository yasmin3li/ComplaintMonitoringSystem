package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.RoleRepo;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Role;
import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.exceptionHandller.ApiException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepo accountRepo;
    private final RoleRepo roleRepo;

    @Transactional
    public ApiResponseDto<?> enable(Long accountId) {

        Optional<Account> employeeAccount = accountRepo.findById(accountId);
        if(employeeAccount.isEmpty()){
            throw new ApiException("no account", HttpStatus.NOT_FOUND);
        }
        employeeAccount.get().setStatus(AccountStatus.ACTIVATED);
        return new ApiResponseDto<>(
                true,
                "account for employee "+employeeAccount.get().getUserName()+" enabled successfully",
                null);
    }

    @Transactional
    public ApiResponseDto<?> disable(Long accountId) {

        Optional<Account> employeeAccount = accountRepo.findById(accountId);
        if(employeeAccount.isEmpty()){
            throw new ApiException("no account", HttpStatus.NOT_FOUND);
        }
        employeeAccount.get().setStatus(AccountStatus.BANNED);
        return new ApiResponseDto<>(
                true,
                "account for employee "+employeeAccount.get().getUserName()+" disabled successfully",
                null);
    }

    @Transactional
    public String accountStatus(Long accountId) {

        Optional<Account> employeeAccount = accountRepo.findById(accountId);
        if(employeeAccount.isEmpty()){
            throw new ApiException("no account", HttpStatus.NOT_FOUND);
        }

        return employeeAccount.get().getStatus().toString();

    }

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

}

