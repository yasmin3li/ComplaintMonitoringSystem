package com.myapp.complaints.config.jwtAuth;


import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.entity.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenGenerator {

    private final AccountRepo accountRepo ;
    private final JwtEncoder jwtEncoder;

    public String generateAccessToken(Authentication authentication) {

       log.warn("[JwtTokenGenerator:generateAccessToken] Token Creation Started for:{}", authentication.getName());
        Account account = accountRepo.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Account not found for: " + authentication.getName())
                );
       log.warn(account.toString());

        String roles = getRolesOfUser(authentication);

        String permissions = getPermissionsFromRoles(roles);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("YOUR_VOICE")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .subject(authentication.getName())
                .claim("scope", permissions)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(Authentication authentication) {

//        log.warn("[JwtTokenGenerator:generateRefreshToken] Token Creation Started for:{}", authentication.getName());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("YOUR_VOICE")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(15 , ChronoUnit.DAYS))
                .subject(authentication.getName())
                .claim("scope", "REFRESH_TOKEN")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
    private static String getRolesOfUser(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
    }

    private String getPermissionsFromRoles(String roles) {
        Set<String> permissions = new HashSet<>();

        if (roles.contains("ROLE_ADMIN")) {
            permissions.addAll(List.of("READ", "WRITE", "DELETE"));
        }
        if (roles.contains("ROLE_MANAGER")) {
            permissions.addAll(List.of("READ", "WRITE"));
        }
        if (roles.contains("ROLE_CITIZEN")) {
            permissions.add("READ");
        }
        if (roles.contains("ROLE_RECEPTIONIST")) {
            permissions.add("READ");
        }
        return String.join(" ", permissions);
    }

}
