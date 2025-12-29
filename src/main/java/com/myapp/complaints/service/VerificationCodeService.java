package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.VerificationCodeRepo;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.VerificationCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final AccountRepo accountRepo;
    private final VerificationCodeRepo verificationCodeRepo;
    private final Random random = new Random();

    public String generateCode(Account account,String type) {

        // generate random code with length : 6
        String code = String.format("%06d", random.nextInt(1000000));

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setAccount(account);
        verificationCode.setVerificationCode(code);
        verificationCode.setVerificationCodeExpireTime(LocalDateTime.now().plusMinutes(10));

        verificationCodeRepo.save(verificationCode);

        if("EMAIL".equals(type)) {
            sendCodeToEmail(account.getEmail(), code);
        } else if("SMS".equals(type)) {
            sendCodeToPhone(account.getPhoneNumber(), code);
        }
        return code;
    }

    private void sendCodeToEmail(String email, String code) {
        System.out.println("Send OTP " + code + " to email " + email);
//TODO: add email provider
    }
    public boolean validateCode(Account account, String code) {
        return verificationCodeRepo.findByAccountAndVerificationCode(account, code)
                .map(verificationCode -> {
                    if(verificationCode.getVerificationCodeExpireTime().isAfter(LocalDateTime.now())) {
                        verificationCodeRepo.delete(verificationCode); //the code is valid only once
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    private void sendCodeToPhone(String phoneNumber, String code) {
//TODO: add SMS provider
        System.out.println("Send OTP " + code + " to phone " + phoneNumber);
    }
}

