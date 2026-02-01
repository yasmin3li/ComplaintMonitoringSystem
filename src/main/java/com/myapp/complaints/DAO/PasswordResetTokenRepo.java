package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.PasswordResetToken;
import com.myapp.complaints.entity.VerificationCode;
import com.myapp.complaints.enums.CodeAndLinkState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken,Long> {

    Optional<PasswordResetToken> findByTokenAndAccount(String token,Account account);

    void deleteByAccount(Account account);

    int countByAccountAndExpiryDateAfter(
            Account account,
            LocalDateTime time
    );

    List<PasswordResetToken> findByAccountAndState(
            Account account,
            CodeAndLinkState state
    );

}
