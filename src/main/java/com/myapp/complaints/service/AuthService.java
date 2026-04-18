package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.*;
import com.myapp.complaints.config.jwtAuth.JwtTokenGenerator;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.*;
import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.enums.TokenType;
import com.myapp.complaints.exceptionHandller.ApiException;
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

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final GovernorateRepo governorateRepo;
    private final SectorRepo sectorRepo;
    private final JwtEncoder jwtEncoder;
    private final AccountRepo accountRepo;
    private final CitizenRepo citizenRepo;
    private final EmployeeRepo employeeRepo;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenRepo refreshTokenRepo;
    private final AccountInfoMapper accountInfoMapper;
    private final InstitutionRepo institutionRepo;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepo roleRepo;
    private  final  VerificationCodeService verificationCodeService;
    private  final SectorGovernorateRepo sectorGovernorateRepo;
    private final InstitutionSectorGovernorateRepo institutionSectorGovernorateRepo;

    //        private final PasswordResetTokenRepo passwordResetTokenRepo;
//    private final UserInfoMapper userInfoMapper;
//        private final EmailService emailService;
//        private final VerificationCodeRepo verificationCodeRepo ;
//        private final BCryptPasswordEncoder bCryptPasswordEncoder;
//        private final Random random ;
    @Transactional
    public AuthResponseDto getJwtTokensAfterAuthentication(Authentication authentication, HttpServletResponse response) {

        var userInfoEntity = accountRepo.findByEmailAndDeletedFalse(authentication.getName())
                .orElseThrow(() -> {
                    AuthService.log.error("[AuthService:userSignInAuth] User :{} not found", authentication.getName());
                    return new ApiException("USER NOT FOUND ", HttpStatus.NOT_FOUND);
                });

        if(userInfoEntity.getStatus().equals(AccountStatus.PENDING)){
            log.warn("Login attempt for pending account: {}", userInfoEntity.getEmail());
            throw new ApiException("your account not verified yet",HttpStatus.FORBIDDEN);
        }

//        try {
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


//        } catch (Exception e) {
//            AuthService.log.error("[AuthService:userSignInAuth]Exception while authenticating the user due to :" + e.getMessage());
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
//        }
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
                        new ApiException("Refresh token revoked",HttpStatus.INTERNAL_SERVER_ERROR));

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
                            default -> throw new ApiException("Unknown role: " + role,HttpStatus.BAD_REQUEST);
                        })
                        .toList();

        return new UsernamePasswordAuthenticationToken(username, password, authorities);
    }


    @Transactional
    public ApiResponseDto<?> registerCitizen(CitizenRegistrationDto dto) {

//        Account account = accountRepo.findByNationalNumberAndDeletedTrue(dto.nationalNumber());
//
//        if(account != null){
//            return new ApiResponseDto<>(true,"يوجد حساب من قبل هل تود ان تعيد تفعيله",account.getUserName());
//        }

        Account account = accountInfoMapper.fromCitizenDto(dto);
        ApiResponseDto<Object> codeResponse;
        String type;

        if (dto.email() == null || dto.email().isBlank()) {

            account.setEmail(dto.userName()+dto.phoneNumber().substring(4,9) + "@example.com");
            account.setEmailTemporary(true);
            type="SMS";
 /*
 Account is created even if email sending fails.
 SMTP may accept the message even if the email address does not exist,
 so we cannot always detect invalid emails at this stage.
 If the user does not receive the verification code,
 they can request a new one using the resendVerificationCode() endpoint.
  */
//            codeResponse = verificationCodeService.generateCode(account,"SMS");

        } else {
            account.setEmail(dto.email());
            account.setEmailTemporary(false);
            type="EMAIL";
           // codeResponse = verificationCodeService.generateCode(account,"EMAIL");
        }

////        if account.getRole()==
//        Role citizenRole = roleRepo.findByName("مواطن")
//                .orElseThrow(() -> new RuntimeException("ROLE_CITIZEN not found"));
//        account.setRole(citizenRole);


        String password = account.getPassword();
        if (!password.startsWith("$2a$") && !password.startsWith("$2b$")) {
            if(CommonUtils.validateAndEncodePassword(password))
                account.setPassword(passwordEncoder.encode(password));
        }

//        account.setStatus(AccountStatus.BANNED);
        account = accountRepo.save(account);

        Citizen citizen = new Citizen();
        citizen.setAccount(account);
        citizen.setBirthDate(dto.birthDate());
        citizenRepo.save(citizen);
        codeResponse = verificationCodeService.generateCode(account,type);

//        return account;
        return new ApiResponseDto<>(
                codeResponse.success(),
                "account for user " + account.getUserName() +
                        " created successfully. " + codeResponse.message(),
                codeResponse.data()
        );
    }


    @Transactional
    public ApiResponseDto<?> registerEmployee(@Valid EmployeeRegistrationDto dto) {

        Account account = accountInfoMapper.fromEmployeeDto(dto);

// VALIDATION: prevent inconsistent employee assignment
// Validate that sector belongs to governorate
        boolean sectorExists =
                sectorGovernorateRepo.existsBySectorIdAndGovernorateId(
                        dto.sectorId(),
                        dto.governorateId()
                );

        if (!sectorExists) {
            throw new ApiException("Sector does not belong to the selected governorate",HttpStatus.BAD_REQUEST);
        }

// Validate that institution operates in this sector/governorate
        boolean institutionValid =
                institutionSectorGovernorateRepo
                        .existsByInstitutionIdAndSectorGovernorateSectorIdAndSectorGovernorateGovernorateId(
                                dto.institutionId(),
                                dto.sectorId(),
                                dto.governorateId()
                        );

        if (!institutionValid) {
            throw new ApiException("Institution not valid for this sector/governorate",HttpStatus.BAD_REQUEST);
        }

//        Role role = roleRepo.findById(dto.roleId())
//                .orElseThrow(() -> new RuntimeException("Role not found"));
//        account.setRole(role);

        if (dto.email() == null || dto.email().isBlank()) {
            account.setEmail(dto.userName()+dto.phoneNumber().substring(4,9) + "@temporary.com");
            //verificationCodeService.generateCode(account,"SMS");
            account.setEmailTemporary(true);

        } else {
            account.setEmail(dto.email());
            account.setEmailTemporary(false);
           // verificationCodeService.generateCode(account,"EMAIL");
        }

        account.setMustChangePassword(true);

        String password = account.getPassword();
        if (!password.startsWith("$2a$") && !password.startsWith("$2b$")) {

            if(CommonUtils.validateAndEncodePassword(password))
                account.setPassword(passwordEncoder.encode(password));
        }

        account.setStatus(AccountStatus.SUSPENDED);
        account = accountRepo.save(account);

        Employee employee = new Employee();
        employee.setAccount(account);

        Institution inst = institutionRepo.findById(dto.institutionId())
                .orElseThrow(() -> new ApiException("Institution with id "+dto.institutionId()+" not found",HttpStatus.NOT_FOUND));
        employee.setInstitution(inst);

        Sector sect = sectorRepo.findById(dto.sectorId())
                .orElseThrow(() -> new ApiException("Sector with id "+dto.sectorId()+" not found",HttpStatus.NOT_FOUND));
        employee.setSector(sect);

        Governorate gov = governorateRepo.findById(dto.governorateId())
                .orElseThrow(() -> new ApiException("Governorate with id "+dto.governorateId()+" not found",HttpStatus.NOT_FOUND));
        employee.setGovernorate(gov);

        employeeRepo.save(employee);

//        return account;
        return new ApiResponseDto<>(
                true,
                "account for user "+account.getUserName()+" created successfully",
                null
        );
    }

    private final VerificationCodeRepo verificationCodeRepo;

    @Transactional
    public ApiResponseDto<?> verifyUser(VerifyUserDto dto) {

        Account account;

        if (dto.identifier().contains("@")) {
            account = accountRepo.findByEmailAndDeletedFalse(dto.identifier())
                    .orElseThrow(() -> new ApiException("Account with " + dto.identifier() + " not found", HttpStatus.NOT_FOUND));
        } else {
            account = accountRepo.findByPhoneNumberAndDeletedFalse(dto.identifier())
                    .orElseThrow(() -> new ApiException("Account with " + dto.identifier() + " not found", HttpStatus.NOT_FOUND));
        }

        boolean res = verificationCodeService.validateCode(account, dto.code());

        if (res) {

            account.setStatus(AccountStatus.ACTIVATED);

            if (dto.identifier().contains("@"))
                account.setEmailVerified(true);
            else
                account.setPhoneNumberVerified(true);

            accountRepo.save(account);
        } else
        {
            log.warn("Invalid verification code entered for account: {}", dto.identifier());
             throw new ApiException("invalid code", HttpStatus.BAD_REQUEST);
        }
        return new ApiResponseDto<>(
                true,
                "account verified successfully",
                null
        );
    }

    public Object reActivateCitizenAccount(@Valid CitizenRegistrationDto dto) {

        Account account = accountRepo.findByNationalNumberAndDeletedTrue(dto.nationalNumber());
        account.setNationalNumber(dto.nationalNumber());
        account.setUserName(dto.userName());
        account.setPhoneNumber(dto.phoneNumber());
        account.setPassword(dto.password());

        Role citizenRole = roleRepo.findByName("مواطن")
                .orElseThrow(() -> new ApiException("ROLE_CITIZEN not found", HttpStatus.NOT_FOUND));
        account.setRole(citizenRole);

        ApiResponseDto<Object> codeResponse;
        String type;

        if (dto.email() == null || dto.email().isBlank()) {

            account.setEmail(dto.userName()+dto.phoneNumber().substring(4,9) + "@example.com");
            account.setEmailTemporary(true);
            type="SMS";

        } else {
            account.setEmail(dto.email());
            account.setEmailTemporary(false);
            type="EMAIL";
        }

        String password = account.getPassword();
        if (!password.startsWith("$2a$") && !password.startsWith("$2b$")) {
            if(CommonUtils.validateAndEncodePassword(password))
                account.setPassword(passwordEncoder.encode(password));
        }

        account.setDeleted(false);

        account = accountRepo.save(account);

        Citizen citizen = citizenRepo.findByAccount_Id(account.getId());

        citizen.setAccount(account);
        citizen.setBirthDate(dto.birthDate());
        citizenRepo.save(citizen);
        codeResponse = verificationCodeService.generateCode(account,type);

        return new ApiResponseDto<>(
                codeResponse.success(),
                "account for user reactivated successfully " + account.getUserName() +
                        " created successfully. " + codeResponse.message(),
                codeResponse.data()
        );
    }

}