package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.PasswordResetTokenRepo;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.dto.ChangePasswordRequest;
import com.myapp.complaints.dto.ResetPasswordRequestDTO;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.exceptionHandller.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

//        boolean validLink = restLinkService.validLink(account, dto.token());
//        if (!validLink) {
//            return new ApiResponseDto<>(
//                    false,
//                    "Invalid reset link",
//                    null
//            );
//        }
        if(CommonUtils.validateAndEncodePassword(dto.newPassword()))
            account.setPassword(passwordEncoder.encode(dto.newPassword()));
        //account.setMustChangePassword(false);
        //account.setStatus(AccountStatus.ACTIVATED);
        accountRepo.save(account);
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



