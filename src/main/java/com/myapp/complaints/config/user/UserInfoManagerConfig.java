package com.myapp.complaints.config.user;


import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserInfoManagerConfig implements UserDetailsService {
// instead of directly inject it into security config

    private final AccountRepo userInfoRepo;
    @Override
    //load the user from user details that is (load as) authentication object
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Optional<Account> userOpt = userInfoRepo.findByEmail(userName);

        if(userOpt.isEmpty()) {
            userOpt = userInfoRepo.findByPhoneNumber(userName); // تجربة البحث برقم المستخدم بحال لم يكن لديه ايميل
        }

        return userOpt
                .map(UserInfoConfig::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or username: " + userName));
    }

}

//basically load the user from db
