package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.VerificationCodeRepo;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.VerificationCode;
import com.myapp.complaints.enums.VerificationChannel;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final AccountRepo accountRepo;
    private final VerificationCodeRepo verificationCodeRepo;
    private final Random random = new Random();
    private final EmailService emailService;

    public String generateCode(Account account, String type) {

        // generate random code with length : 6
        String code = String.format("%06d", random.nextInt(1000000));

        VerificationCode verificationCode = VerificationCode.builder()
                .account(account)
                .verificationCode(code)
                .type(type)
                .verificationCodeExpireTime(LocalDateTime.now().plusMinutes(10))
                .isUsed(false)
                .build();

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
        emailService.sendVerificationCode(email,code);
//TODO: add email provider
    }

    private void sendCodeToPhone(String phoneNumber, String code) {
//TODO: add SMS provider
        System.out.println("Send OTP " + code + " to mobile " + phoneNumber);
//        Twilio.init(accountSid, authToken);
//
//        Message.creator(
//                new PhoneNumber(phoneNumber),   // TO
//                new PhoneNumber(fromNumber),    // FROM
//                "Your verification code is: " + code
//        ).create();
//
//        System.out.println("SMS sent to " + phoneNumber);

//        try {
//            RestTemplate restTemplate = new RestTemplate();
//            String url = "https://textbelt.com/text";
//
//            Map<String, String> requestBody = Map.of(
//                    "phone", phoneNumber,
//                    "message", "Your verification code is: " + code,
//                    "key", "textbelt"
//            );
//            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
//            Map body = response.getBody();
//            if(body != null && Boolean.TRUE.equals(body.get("success"))) {
//                System.out.println("SMS sent successfully to " + phoneNumber);
//            } else {
//                System.out.println("Failed to send SMS: " + body);
//            }
//
//        } catch (Exception e) {
//            System.out.println("Error sending SMS: " + e.getMessage());
//        }
    }


    public boolean validateCode(Account account, String code) {

        return verificationCodeRepo.findByAccountAndVerificationCode(account, code)
                .map(verificationCode -> {
                    if(verificationCode.getVerificationCodeExpireTime().isAfter(LocalDateTime.now()) & !verificationCode.isUsed()) {
                        verificationCode.setUsed(true);
//                        verificationCodeRepo.delete(verificationCode); //the code is valid only once
                        verificationCodeRepo.save(verificationCode);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }


}

