package com.myapp.complaints.service;

import com.myapp.complaints.DAO.*;
import com.myapp.complaints.config.jwtAuth.JwtTokenGenerator;
import com.myapp.complaints.dto.AuthResponseDto;
import com.myapp.complaints.dto.CitizenRegistrationDto;
import com.myapp.complaints.dto.EmployeeRegistrationDto;
import com.myapp.complaints.dto.VerifyUserDto;
import com.myapp.complaints.entity.*;
import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.enums.TokenType;
import com.myapp.complaints.mapper.AccountInfoMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtEncoder jwtEncoder;
    private final AccountRepo accountRepo;
    private final CitizenRepo citizenRepo;
    private final EmployeeRepo employeeRepo;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenRepo refreshTokenRepo;
    private final AccountInfoMapper accountInfoMapper;


    //        private final PasswordResetTokenRepo passwordResetTokenRepo;
//    private final UserInfoMapper userInfoMapper;
//        private final EmailService emailService;
//        private final VerificationCodeRepo verificationCodeRepo ;
//        private final BCryptPasswordEncoder bCryptPasswordEncoder;
//        private final Random random ;
    @Transactional
    public AuthResponseDto getJwtTokensAfterAuthentication(Authentication authentication, HttpServletResponse response) {
        try {
            var userInfoEntity = accountRepo.findByEmail(authentication.getName())
                    .orElseThrow(() -> {
                        AuthService.log.error("[AuthService:userSignInAuth] User :{} not found", authentication.getName());
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "USER NOT FOUND ");
                    });


            String accessToken = jwtTokenGenerator.generateAccessToken(authentication);
            String refreshToken = jwtTokenGenerator.generateRefreshToken(authentication);
            //Let's save the refreshToken as well
            saveUserRefreshToken(userInfoEntity, refreshToken);
            //Creating the cookie
            createRefreshTokenCookie(response, refreshToken);
            AuthService.log.warn("[AuthService:userSignInAuth] Access token for user:{}, has been generated", userInfoEntity.getEmail());
            return AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .accessTokenExpiry(String.valueOf(15 * 60))
                    .userName(userInfoEntity.getUserName())
                    .tokenType(TokenType.BEARER)
                    .build();


        } catch (Exception e) {
            AuthService.log.error("[AuthService:userSignInAuth]Exception while authenticating the user due to :" + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Please Try Again");
        }
    }

    @Transactional
    private void saveUserRefreshToken(Account userInfoEntity, String refreshToken) {
        var refreshTokenEntity = RefreshToken.builder()
                .refreshToken(refreshToken)
                .account(userInfoEntity)
                .revoked(false)
                .build();
        refreshTokenRepo.save(refreshTokenEntity);

    }


    private void createRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true) // Enable in production
                .maxAge(15 * 24 * 60 * 60) // 15 days
                .path("/auth/refresh-token")
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    @Transactional
    public Object getAccessTokenUsingRefreshToken(HttpServletRequest httpServletRequest) {

        // 1. Extract refresh token from HttpOnly cookie (not headers!)
        String refreshToken = Arrays.stream(httpServletRequest.getCookies())
                .filter(c -> c.getName().equals("refresh_token"))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("Refresh token missing"));


        RefreshToken refreshTokenEntity = refreshTokenRepo.findByRefreshToken(refreshToken)
                .filter(tokens -> !tokens.isRevoked())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Refresh token revoked"));

        Account userInfoEntity = refreshTokenEntity.getAccount();

        Authentication authentication = createAuthenticationObject(userInfoEntity);

        //Use the authentication object to generate new accessToken as the Authentication object that we will have may not contain correct role.
        String accessToken = jwtTokenGenerator.generateAccessToken(authentication);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .accessTokenExpiry(String.valueOf(15 * 60))
                .userName(userInfoEntity.getUserName())
                .tokenType(TokenType.BEARER)
                .build();
    }

    @Transactional
    private static Authentication createAuthenticationObject(Account userInfoEntity) {
        String username = userInfoEntity.getEmail();
        String password = userInfoEntity.getPassword();

        List<SimpleGrantedAuthority> authorities =
                Arrays.stream(userInfoEntity.getRole().getName().split(","))
                        .map(String::trim)
                        .map(role -> switch (role) {
                            case "مواطن" -> new SimpleGrantedAuthority("ROLE_USER");
                            case "موظف الاستقبال" -> new SimpleGrantedAuthority("ROLE_RECEPTIONIST");
                            case "مدير" -> new SimpleGrantedAuthority("ROLE_MANAGER");
                            case "أدمن" -> new SimpleGrantedAuthority("ROLE_ADMIN");
                            default -> throw new IllegalArgumentException("Unknown role: " + role);
                        })
                        .toList();

        return new UsernamePasswordAuthenticationToken(username, password, authorities);
    }
    
    private final InstitutionRepo institutionRepo;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepo roleRepo;
    private  final  VerificationCodeService verificationCodeService;
    @Transactional
    public Account registerCitizen(CitizenRegistrationDto dto) {


        Account account = accountInfoMapper.fromCitizenDto(dto);


        Role citizenRole = roleRepo.findByName("مواطن")
                .orElseThrow(() -> new RuntimeException("ROLE_CITIZEN not found"));
        account.setRole(citizenRole);

        if (!account.isEmailTemporary())
            verificationCodeService.generateCode(account,"EMAIL");
        else
            verificationCodeService.generateCode(account,"SMS");

        account = accountRepo.save(account);


        Citizen citizen = new Citizen();
        citizen.setAccount(account);
        citizen.setBirthDate(dto.birthDate());
        citizenRepo.save(citizen);

        return account;
    }


    @Transactional
    public Account registerEmployee(@Valid EmployeeRegistrationDto dto) {


        Account account = accountInfoMapper.fromEmployeeDto(dto);


        Role role = roleRepo.findById(dto.roleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        account.setRole(role);

        if (!account.isEmailTemporary())
            verificationCodeService.generateCode(account,"EMAIL");
        else
            verificationCodeService.generateCode(account,"SMS");

        account = accountRepo.save(account);

        account = accountRepo.save(account);


        Employee employee = new Employee();
        employee.setAccount(account);

        Institution inst = institutionRepo.findById(dto.institutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));
        employee.setInstitution(inst);

        employeeRepo.save(employee);

        return account;
    }

    private final VerificationCodeRepo verificationCodeRepo;
    public boolean verifyUser(@Valid VerifyUserDto verifyUserDto) {
        String email=verifyUserDto.email();
        Account account=accountRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("this account "+verifyUserDto.email()+" not found"));
        VerificationCode verificationCode =
                verificationCodeRepo.findByAccountAndVerificationCode(account, verifyUserDto.code())
                        .orElseThrow(() ->
                                new RuntimeException("Invalid verification code") );

        if (verificationCode.isUsed()) {
            throw new RuntimeException("Verification code already used");
        }

        if (verificationCode.getVerificationCodeExpireTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code expired");
        }


        account.setEmailVerified(true);
        account.setStatus(AccountStatus.ACTIVATED);

        verificationCode.setUsed(true);
        accountRepo.save(account);
        verificationCodeRepo.save(verificationCode);

        return true;

    }
}