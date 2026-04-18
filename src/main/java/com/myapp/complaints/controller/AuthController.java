package com.myapp.complaints.controller;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.service.*;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final EntityManager entityManager;
    private final ApiService apiService;
    private final VerificationCodeService verificationCodeService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;
    private final RestLinkService restLinkService;


//auth/sign-in
    @PostMapping("/sign-in")
    public ResponseEntity<?> authenticateUser(Authentication authentication, HttpServletResponse response){

        return ResponseEntity.ok(authService.getJwtTokensAfterAuthentication(authentication,response));//authentication obj
    }

//
    //    @PreAuthorize("hasAuthority('SCOPE_REFRESH_TOKEN')")
    @PostMapping ("/refresh-token")
    public ResponseEntity<?> getAccessToken(HttpServletRequest httpServletRequest){

        return ResponseEntity.ok(authService.getAccessTokenUsingRefreshToken(httpServletRequest));
        //TODO add refresh token in the httponly
    }


    @PostMapping("/sign-up/citizen")
    public ResponseEntity<?> registerCitizen(@Valid @RequestBody CitizenRegistrationDto dto) {
        return ResponseEntity.ok(authService.registerCitizen(dto));
    }

    @PostMapping("/citizen/account/re-activate")
    public ResponseEntity<?> reActivateAccount(@Valid @RequestBody CitizenRegistrationDto dto) {
        return ResponseEntity.ok(authService.reActivateCitizenAccount(dto));
    }

    @PostMapping("/sign-up/employee")
    public ResponseEntity<?> registerEmployee(@Valid @RequestBody EmployeeRegistrationDto dto) {
        return ResponseEntity.ok(authService.registerEmployee(dto));
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request) {

//        verificationCodeService.generateCode(request.emailOrPhone(),);

        return ResponseEntity.ok(restLinkService.sendResetLink(request));
    }

    @PostMapping("/verify/reset-password-link")
    public ResponseEntity<?> verifyResetPassword(
            @Valid @RequestBody VerifyUserDto verifyUserDto) {
        return ResponseEntity.ok(restLinkService.validLink(verifyUserDto.identifier(),verifyUserDto.code()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request) {
//        passwordService.resetPassword(request);
        return ResponseEntity.ok( passwordService.resetPassword(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
           Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

//        passwordService.changePassword(authentication,request);
        return ResponseEntity.ok(passwordService.changePassword(authentication,request));
    }


    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser( @Valid @RequestBody VerifyUserDto verifyUserDto) {
//        authService.verifyUser(verifyUserDto);
//        return ResponseEntity.ok("Account verified successfully");
        return ResponseEntity.ok(authService.verifyUser(verifyUserDto));
    }


    @PostMapping("/resend/code")
    public ResponseEntity<?> resendVerificationCode( @RequestBody ForgotPasswordRequestDTO emailOrPhone) throws MessagingException {
//        verificationCodeService.resendVerificationCode(emailOrPhone);
//        return ResponseEntity.ok("Verification code sent");
        return ResponseEntity.ok(verificationCodeService.resendVerificationCode(emailOrPhone));
    }

    @PostMapping("/resend/link")
    public ResponseEntity<?> resendResetLink( @RequestBody ForgotPasswordRequestDTO emailOrPhone) throws MessagingException {
//        restLinkService.resendRestLink(emailOrPhone);
        return ResponseEntity.ok(restLinkService.resendRestLink(emailOrPhone));
    }

    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ResponseEntity.ok(apiService.deleteAccount(email));
    }

}

