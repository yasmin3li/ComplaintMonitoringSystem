package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken,Long> {

    Optional<PasswordResetToken> findByTokenAndAccount(String token,Account account);

    void deleteByAccount(Account account);

}
