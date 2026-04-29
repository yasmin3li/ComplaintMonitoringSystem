package com.myapp.complaints.mapper;


import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.CitizenRepo;
import com.myapp.complaints.DAO.RoleRepo;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Citizen;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.entity.Role;
import com.myapp.complaints.exceptionHandller.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AccountInfoMapper {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepo roleRepo;
    private final AccountRepo accountRepo;
    private final CitizenRepo citizenRepo;

    public Account fromCitizenDto(CitizenRegistrationDto dto) {
        Account account = new Account();
        account.setUserName(dto.userName());
        account.setPhoneNumber(dto.phoneNumber());
        account.setNationalNumber(dto.nationalNumber());
        account.setPassword(dto.password());

        Role citizenRole = roleRepo.findByName("مواطن")
                .orElseThrow(() -> new ApiException("ROLE_CITIZEN not found", HttpStatus.NOT_FOUND));
        account.setRole(citizenRole);


//        if (dto.email() == null || dto.email().isBlank()) {
//            String phone = dto.phoneNumber() != null ? dto.phoneNumber() : "user" + System.currentTimeMillis();
//            account.setEmail(phone + "@example.com");
//            account.setEmailTemporary(true);
//        } else {
//            account.setEmail(dto.email());
//            account.setEmailTemporary(false);
//        }

        return account;
    }

    public Account fromEmployeeDto(EmployeeRegistrationDto dto) {
        Account account = new Account();
        account.setUserName(dto.userName());
        account.setPhoneNumber(dto.phoneNumber());
        account.setNationalNumber(dto.nationalNumber());
        account.setPassword(dto.password());

        Role role = roleRepo.findById(dto.roleId())
                .orElseThrow(() -> new ApiException("Role not found",HttpStatus.NOT_FOUND));
        account.setRole(role);

//        if (dto.email() == null || dto.email().isBlank()) {
//           // String phone = dto.phoneNumber() != null ? dto.phoneNumber() : "user" + System.currentTimeMillis();
//           // account.setEmail(phone + "@example.com");
//            account.setEmail(dto.userName()+dto.phoneNumber().substring(4,9) + "@example.com");
//
//            account.setEmailTemporary(true);
//        } else {
//            account.setEmail(dto.email());
//            account.setEmailTemporary(false);
//        }

        return account;
    }

    public CitizenProfileInfoDto citizenInfoToDto(Citizen citizen){

        return  new CitizenProfileInfoDto(
                citizen.getAccount().getUserName(),
                citizen.getAccount().getEmail(),
                citizen.getAccount().getPhoneNumber(),
                citizen.getAccount().getNationalNumber(),
                citizen.getAccount().isEmailTemporary(),
                citizen.getAccount().getProfileImageUrl(),
                citizen.getAccount().getCreatedAt(),
                citizen.getAccount().getUpdatedAt(),
                citizen.getBirthDate()
        );
    }

    public EmployeeProfileInfoDto employeeInfoToDto(Employee employee) {
        return new EmployeeProfileInfoDto(
                employee.getAccount().getUserName(),
                employee.getAccount().isEmailTemporary(),
                employee.getAccount().getEmail(),
                employee.getAccount().getPhoneNumber(),
                employee.getAccount().getProfileImageUrl(),
                employee.getAccount().getCreatedAt(),
                employee.getAccount().getUpdatedAt(),
                employee.getInstitution(),
                employee.getGovernorate(),
                employee.getSector()
        );
    }

//TODO : dealing with employee's account
    public void updateAccountFromDto(UpdateCitizenProfileInfoDto dto, Citizen citizen) {

        if (dto.userName() != null) {
            citizen.getAccount().setUserName(dto.userName());
        }

        if (dto.profileImageUrl() != null) {
            citizen.getAccount().setProfileImageUrl(dto.profileImageUrl());
        }

        if (dto.birthDate() != null) {
            citizen.setBirthDate(dto.birthDate());
        }
        citizen.getAccount().setUpdatedAt(LocalDateTime.now());
    }

    public void updateAccountFromDto(UpdateEmployeeProfileInfoDto dto, Employee employee) {

        if (dto.userName() != null) {
            employee.getAccount().setUserName(dto.userName());
        }

        if (dto.profileImageUrl() != null) {
            employee.getAccount().setProfileImageUrl(dto.profileImageUrl());
        }

        employee.getAccount().setUpdatedAt(LocalDateTime.now());
    }
}
