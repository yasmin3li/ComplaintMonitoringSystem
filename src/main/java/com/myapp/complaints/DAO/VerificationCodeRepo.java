package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepo extends JpaRepository<VerificationCode,Long> {
    Optional<VerificationCode> findByAccountAndVerificationCode(Account account, String code);
}
