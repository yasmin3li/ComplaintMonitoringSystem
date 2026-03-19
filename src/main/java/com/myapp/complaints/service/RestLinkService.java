package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.PasswordResetTokenRepo;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.dto.ForgotPasswordRequestDTO;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.PasswordResetToken;
import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.enums.CodeAndLinkState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class RestLinkService {
    private final EmailService emailService;
    private final AccountRepo accountRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;

    public ApiResponseDto<Object> sendResetLink(ForgotPasswordRequestDTO dto) {

        Account account = accountRepo.findByEmail(dto.emailOrPhone())
                .orElseGet(() ->
                        accountRepo.findByPhoneNumber(dto.emailOrPhone())
                                .orElse(null)
//                                .orElseThrow(() ->
//                                        new RuntimeException("Account not found")
//                                )
                );
        if (account == null) {
            return new ApiResponseDto<Object>(false,"Account not found",null);
         }

// limit trays
        int attemptsLastHour =
                passwordResetTokenRepo.countByAccountAndExpiryDateAfter(
                        account,
                        LocalDateTime.now().minusHours(1)
                );

        if (attemptsLastHour >= 2) {
//            throw new RuntimeException("You have exceeded the limit. Try again after 1 hour");
            return new ApiResponseDto<>(
                    false,
                    "You have exceeded the limit. Try again after 1 hour",
                    null
            );
            }

        //to make unused codes and invalid "INVALID" rather thn "UNUSED"
        List<PasswordResetToken> activeCodes =
                passwordResetTokenRepo.findByAccountAndState(
                        account,
                        CodeAndLinkState.UNUSED
                );

        for (PasswordResetToken code : activeCodes) {
            code.setState(CodeAndLinkState.INVALID);
        }
        passwordResetTokenRepo.saveAll(activeCodes);


//add the tpe of code (SMS,EMAIL)
        String type;
        if (account.isEmailTemporary()){
            type="SMS";}
        else{
            type="EMAIL";}

//Build the reset link
//            passwordResetTokenRepo.deleteByAccount(account);

            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setAccount(account);
            resetToken.setType(type);
            resetToken.setState(CodeAndLinkState.UNUSED);
            resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

            passwordResetTokenRepo.save(resetToken);

        if (resetToken.getType().equals("EMAIL")) {
            System.out.println("reset token send to your email and it is  "+token);
            boolean value;

//TODO after link with front
                value = emailService.sendResetLink(
                        account.getEmail(),
                        "Reset your password for Balligh _ بَلِّغْ ",
                        "Hello "+account.getUserName()+" \nClick the link bellow to reset your password :\n" +
                                "http://localhost:5173/reset-password?token=" + token
                );

            if (value){
                return new ApiResponseDto<>(
                        true,
                        "reset link sent to your email",
                        null
                );
            }
            else {
                return new ApiResponseDto<>(
                        false,
                        "failed to send rest link ",
                        null
                );
            }
        }
        else {
            System.out.print("reset link send to yor phone number and it is:  "+token);
            return new ApiResponseDto<>(
                    false,
                    "reset link sent to yor phone number",
                    null
            );
    }
    }

    public boolean validLink(Account account, String token) {

        return passwordResetTokenRepo.findByTokenAndAccount(token,account)
                .map(resetToken -> {
                    if(resetToken.getExpiryDate().isAfter(LocalDateTime.now()) && resetToken.getState().equals(CodeAndLinkState.UNUSED)) {

//TODO: move this logic from service to another place
                        resetToken.setState(CodeAndLinkState.USED);
                        passwordResetTokenRepo.save(resetToken);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }



    public ApiResponseDto<Object> resendRestLink(ForgotPasswordRequestDTO emailOrPhone) {
        Account account = accountRepo.findByEmailAndStatus(emailOrPhone.emailOrPhone(), AccountStatus.ACTIVATED)
                .orElseGet(() ->
//                        TODO: ACTIVATED not PENDING
                        accountRepo.findByPhoneNumberAndStatus(emailOrPhone.emailOrPhone(), AccountStatus.ACTIVATED)
                                .orElse(null)
//                                .orElseThrow(() ->
//                                        new RuntimeException("Account not found")
//                                )
                );
        if (account == null) {
            return new ApiResponseDto<Object>(false,"Account not found",null);
        }
//moved up
//        List<PasswordResetToken> activeCodes =
//                passwordResetTokenRepo.findByAccountAndState(
//                        account,
//                        CodeAndLinkState.UNUSED
//                );
//
//        for (PasswordResetToken code : activeCodes) {
//            code.setState(CodeAndLinkState.INVALID);
//        }
//        passwordResetTokenRepo.saveAll(activeCodes);

//        return  resendRestLink(emailOrPhone);
        return  sendResetLink(emailOrPhone);
    }
    }

