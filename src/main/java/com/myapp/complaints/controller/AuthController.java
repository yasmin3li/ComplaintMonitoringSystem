package com.myapp.complaints.controller;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.service.AuthService;
import com.myapp.complaints.service.PasswordResetService;
import com.myapp.complaints.service.VerificationCodeService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final EntityManager entityManager;
    private final AccountRepo accountRepo;
    private final VerificationCodeService verificationCodeService;
    private final PasswordEncoder passwordEncoder;

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

    @PostMapping("/sign-up/employee")
    public ResponseEntity<?> registerEmployee(@Valid @RequestBody EmployeeRegistrationDto dto) {
        return ResponseEntity.ok(authService.registerEmployee(dto));
    }


    private final PasswordResetService passwordResetService;
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request) {

        passwordResetService.sendResetLink(request);

        return ResponseEntity.ok("reset link has been sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request) {



        passwordResetService.resetPassword(request);


        return ResponseEntity.ok("Password reset successfully");
    }

//    @PostMapping("/change-password")
//    public ResponseEntity<?> changePassword(
//            @AuthenticationPrincipal UserDetails user,
//            @Valid @RequestBody ChangePasswordRequest request) {
//
//        Account account = accountRepo.findByEmail(user.getUsername())
//                .orElseThrow(() -> new RuntimeException("Account not found"));
//
//        if (!account.isMustChangePassword()) {
//            if (request.currentPassword() == null ||
//                    !passwordEncoder.matches(request.currentPassword(), account.getPassword())) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password incorrect");
//            }
//        }
//
//        account.setPassword(validateAndEncodePassword(request.newPassword()));
//        account.setMustChangePassword(false);
//        account.setStatus(AccountStatus.ACTIVATED);
//        accountRepo.save(account);
//
//        return ResponseEntity.ok("Password changed successfully");
//    }




    // email verify
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser( @Valid @RequestBody VerifyUserDto verifyUserDto) {
        authService.verifyUser(verifyUserDto);
        return ResponseEntity.ok("Account verified successfully");
    }



//    @PostMapping("/resend")
//    public ResponseEntity<?> resendVerificationCode( @RequestParam String email) throws MessagingException {
//        authService.resendVerificationCode(email);
//        return ResponseEntity.ok("Verification code sent");
//    }
//


}

