package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.PasswordResetTokenRepo;
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

    public void sendResetLink(ForgotPasswordRequestDTO dto) {

        Account account = accountRepo.findByEmail(dto.emailOrPhone())
                .orElseGet(() ->
                        accountRepo.findByPhoneNumber(dto.emailOrPhone())
                                .orElseThrow(() ->
                                        new RuntimeException("Account not found")
                                )
                );

// limit trays
        int attemptsLastHour =
                passwordResetTokenRepo.countByAccountAndExpiryDateAfter(
                        account,
                        LocalDateTime.now().minusHours(1)
                );

        if (attemptsLastHour >= 2) {
            throw new RuntimeException("You have exceeded the limit. Try again after 1 hour");}

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
            System.out.println("reset token send to your email and it is  "+resetToken);
//TODO after link with front
            try {
                emailService.sendResetLink(
                        account.getEmail(),
                        "Reset your password - Your Voice",
                        "Click the link:\n" +
                                "http://localhost:5173/reset-password?token=" + token
                );
            } catch (RuntimeException e) {
                throw new RuntimeException("failed to send rest link ");
            }
        }
        else {
            System.out.print("reset link send to yor phone number and it is:  "+token);
    }
    }

    public boolean validLink(Account account, String token) {

        return passwordResetTokenRepo.findByTokenAndAccount(token,account)
                .map(resetToken -> {
                    if(resetToken.getExpiryDate().isAfter(LocalDateTime.now()) & !resetToken.getState().equals(CodeAndLinkState.UNUSED)) {
                        resetToken.setState(CodeAndLinkState.USED);
                        passwordResetTokenRepo.save(resetToken);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }



    public void resendRestLink(ForgotPasswordRequestDTO emailOrPhone) {
        Account account = accountRepo.findByEmailAndStatus(emailOrPhone.emailOrPhone(), AccountStatus.PENDING)
                .orElseGet(() ->
                        accountRepo.findByPhoneNumberAndStatus(emailOrPhone.emailOrPhone(), AccountStatus.PENDING)
                                .orElseThrow(() ->
                                        new RuntimeException("Account not found")
                                )
                );
        List<PasswordResetToken> activeCodes =
                passwordResetTokenRepo.findByAccountAndState(
                        account,
                        CodeAndLinkState.UNUSED
                );

        for (PasswordResetToken code : activeCodes) {
            code.setState(CodeAndLinkState.INVALID);
        }
        passwordResetTokenRepo.saveAll(activeCodes);

        resendRestLink(emailOrPhone);

    }
    }

