package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.PasswordResetTokenRepo;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.dto.ChangePasswordRequest;
import com.myapp.complaints.dto.ResetPasswordRequestDTO;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.PasswordResetToken;
import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.enums.CodeAndLinkState;
import com.myapp.complaints.exceptionHandller.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final AccountRepo accountRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final RestLinkService restLinkService;


    /**
     * Reset password using token
     */
    @Transactional
    public ApiResponseDto<Object> resetPassword(ResetPasswordRequestDTO dto) {

        Account account = accountRepo.findByEmailAndDeletedFalse(dto.emailOrPhone())
                .orElseGet(() ->
                        accountRepo.findByPhoneNumberAndDeletedFalse(dto.emailOrPhone())
                                .orElseThrow(() ->
                                        new ApiException( "Account not found",HttpStatus.NOT_FOUND))
                );

//        if(!restLinkService.validLink(account.getEmail(), dto.token()).success()){
//
//            return new ApiResponseDto<>(
//                    false,
//                    "Invalid reset link",
//                    null
//            );
//        }

        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepo.findByTokenAndAccount_Email(dto.token(), account.getEmail());

        if(passwordResetToken.isEmpty()){
            passwordResetToken = passwordResetTokenRepo.findByTokenAndAccount_PhoneNumber(dto.token(), account.getEmail());
        }

        if(passwordResetToken.isPresent()){
            if (passwordResetToken.get().getState() != CodeAndLinkState.UNUSED ||
                    passwordResetToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new ApiException("Token expired", HttpStatus.BAD_REQUEST);
            }

            passwordResetToken.get().setState(CodeAndLinkState.USED);
            passwordResetTokenRepo.save(passwordResetToken.get());

            if(CommonUtils.validateAndEncodePassword(dto.newPassword()))
                account.setPassword(passwordEncoder.encode(dto.newPassword()));
            //account.setMustChangePassword(false);
            //account.setStatus(AccountStatus.ACTIVATED);
            accountRepo.save(account);
        }
        return new ApiResponseDto<>(
                true,
                "Password reset successfully",
                null
        );
//        passwordResetTokenRepo.delete(resetToken);
    }

    @Transactional
    public ApiResponseDto<?> changePassword(Authentication auth, ChangePasswordRequest req) {

        Account account = accountRepo.findByEmailAndDeletedFalse(auth.getName())
                .orElseThrow(() ->
                        new ApiException( "Account not found",HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(req.currentPassword(), account.getPassword())) {

            throw new ApiException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        if(CommonUtils.validateAndEncodePassword(req.newPassword()))
            account.setPassword(passwordEncoder.encode(req.newPassword()));
        account.setStatus(AccountStatus.ACTIVATED);
        account.setMustChangePassword(false);
        accountRepo.save(account);

        return new ApiResponseDto<>(true, "Password changed successfully", null);
    }

}



