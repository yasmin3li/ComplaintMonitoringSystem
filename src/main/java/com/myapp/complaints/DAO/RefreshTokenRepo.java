package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByRefreshToken(String tokenValue);
}
