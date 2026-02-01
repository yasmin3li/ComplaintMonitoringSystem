package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.VerificationCode;
import com.myapp.complaints.enums.CodeAndLinkState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VerificationCodeRepo extends JpaRepository<VerificationCode,Long> {
    Optional<VerificationCode> findByAccountAndVerificationCode(Account account, String code);

    int countByAccountAndVerificationCodeExpireTimeAfter(
            Account account,
            LocalDateTime time
    );

    List<VerificationCode> findByAccountAndState(
            Account account,
            CodeAndLinkState state
    );
}
