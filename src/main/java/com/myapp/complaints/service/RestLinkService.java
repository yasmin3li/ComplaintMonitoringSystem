package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.PasswordResetTokenRepo;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.dto.ForgotPasswordRequestDTO;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.PasswordResetToken;
import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.enums.CodeAndLinkState;
import com.myapp.complaints.exceptionHandller.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RestLinkService {
    private final EmailService emailService;
    private final AccountRepo accountRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final Random random = new Random();

    public ApiResponseDto<?> sendResetLink(ForgotPasswordRequestDTO dto) {

        Account account = accountRepo.findByEmailAndDeletedFalse(dto.emailOrPhone())
                .orElseGet(() ->
                        accountRepo.findByPhoneNumberAndDeletedFalse(dto.emailOrPhone())
                                .orElseThrow(() ->
                                        new ApiException( "Account not found", HttpStatus.NOT_FOUND))
                );

// limit trays
        int attemptsLastHour =
                passwordResetTokenRepo.countByAccountAndExpiryDateAfter(
                        account,
                        LocalDateTime.now().minusHours(1)
                );

        if (attemptsLastHour >= 2) {
            throw  new ApiException( "You have exceeded the limit. Try again after 1 hour",HttpStatus.FORBIDDEN );
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

//            String token = UUID.randomUUID().toString();
        String token = String.format("%06d", random.nextInt(1000000));


            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setAccount(account);
            resetToken.setType(type);
            resetToken.setState(CodeAndLinkState.UNUSED);
            resetToken.setExpiryDate(LocalDateTime.now().plusDays(15));

            passwordResetTokenRepo.save(resetToken);

        if (resetToken.getType().equals("EMAIL")) {
            System.out.println("reset token send to your email and it is  "+token);
            boolean value;

//TODO after link with front
                value = emailService.
                        sendVerificationCode(account.getEmail(),token);
//                        sendResetLink(
//                        account.getEmail(),
//                        "Reset your password for Balligh _ بَلِّغْ ",
//                        "Hello "+account.getUserName()+" \nClick the link bellow to reset your password :\n" +
//                                "http://localhost:5173/reset-password?token=" + token
//                );

            if (value){
                return new ApiResponseDto<>(
                        true,
                        "reset link sent to your email",
                        isDevMode() && "SMS".equals(type) ? token : null
                );
            }
            else {
                throw  new ApiException( "failed to send rest link ",HttpStatus.INTERNAL_SERVER_ERROR );
            }
        }
        else {
            System.out.print("reset link send to yor phone number and it is:  "+token);
            return new ApiResponseDto<>(
                    true,
                    "reset link sent to yor phone number",
                    isDevMode() && "SMS".equals(type)  ? token : null
            );
    }
    }

    private boolean isDevMode() {
        return true; // لاحقًا تربطها بـ application.properties
    }

//    public boolean validLink(Account account, String token) {
//
//        return passwordResetTokenRepo.findByTokenAndAccount(token,account)
//                .map(resetToken -> {
//                    if(resetToken.getExpiryDate().isAfter(LocalDateTime.now()) && resetToken.getState().equals(CodeAndLinkState.UNUSED)) {
//
////TODO: move this logic from service to another place
//                        resetToken.setState(CodeAndLinkState.USED);
//                        passwordResetTokenRepo.save(resetToken);
//                        return true;
//                    }
//                    return false;
//                })
//                .orElse(false);
//    }
    public ApiResponseDto<?> validLink(String email, String token) {

        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepo.findByTokenAndAccount_Email(token,email);
        if(passwordResetToken.isEmpty()){
            passwordResetToken = passwordResetTokenRepo.findByTokenAndAccount_PhoneNumber(token,email);
        }
        if (passwordResetToken.isPresent()){
            if(passwordResetToken.get().getExpiryDate().isAfter(LocalDateTime.now()) && passwordResetToken.get().getState().equals(CodeAndLinkState.UNUSED)) {

                return new ApiResponseDto<>(
                        true,
                        "reset password verified successfully",
                        null
                );
            }
            else
                throw   new ApiException(  "invalid code",HttpStatus.BAD_REQUEST );
        }

        else
            throw  new ApiException("account with email "+email+" not found",HttpStatus.NOT_FOUND );
    }


    public ApiResponseDto<?> resendRestLink(ForgotPasswordRequestDTO emailOrPhone) {

        accountRepo.findByEmailAndStatusAndDeletedFalse(emailOrPhone.emailOrPhone(), AccountStatus.ACTIVATED)
                .orElseGet(() ->
//                        TODO: ACTIVATED not PENDING
                        accountRepo.findByPhoneNumberAndStatusAndDeletedFalse(emailOrPhone.emailOrPhone(), AccountStatus.ACTIVATED)
                                .orElseThrow(() ->
                                        new ApiException( "Account not found", HttpStatus.NOT_FOUND))
                );

        return  sendResetLink(emailOrPhone);
    }
    }

