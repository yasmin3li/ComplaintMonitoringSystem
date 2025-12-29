package com.myapp.complaints.config.jwtAuth;


import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.config.user.UserInfoConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class JwtTokenUtils {

    private final AccountRepo accountRepo;

    public String getUserName(Jwt jwtToken){
        return jwtToken.getSubject();
    }

    public boolean isTokenValid(Jwt jwtToken, UserDetails userDetails){
        return !isTokenExpired(jwtToken) && jwtToken.getSubject().equals(userDetails.getUsername());
    }

    private boolean isTokenExpired(Jwt jwtToken) {
        return jwtToken.getExpiresAt().isBefore(java.time.Instant.now());
    }

    public UserDetails userDetails(String username){
        return accountRepo.findByEmail(username)
                .map(UserInfoConfig::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
