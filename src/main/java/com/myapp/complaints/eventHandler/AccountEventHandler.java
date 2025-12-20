package com.myapp.complaints.eventHandler;

import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.entity.Account;
//import com.myapp.complaints.service.AccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Transactional
@Slf4j
@Component
@RequiredArgsConstructor
//@AllArgsConstructor
//@NoArgsConstructor
@RepositoryEventHandler(Account.class)
public class AccountEventHandler {


    @HandleBeforeDelete
    public void handleDelete(Account account) {

        // بدل حذف الحساب، نعمل Soft Delete
        account.setDeleted(true);
        account.setStatus(AccountStatus.DEACTIVATED);
        throw new RuntimeException("Physical delete is not allowed. Account has been deactivated.");
    }

    @HandleBeforeCreate
    public void handleCreate(Account account) {
        account.setStatus(AccountStatus.BANNED);
        account.setDeleted(false);
    }

//    private final RoleRepo roleRepository;
//    private final  CitizenRepo citizenRepo;
//    private final  EmployeeRepo employeeRepo;
//    private final  ComplaintRepo complaintRepo;
//    private final AccountService accountService;
//    private final RatingRepo ratingRepository;
//    private final VotingRepo votingRepository;

//    @HandleBeforeCreate
//    public void handleAccountCreate(Account account) {
//        if (account.getRole() == null) {
//            Role defaultRole = roleRepository.findByName("مواطن");
//            account.setRole(defaultRole);
//        }
//    }
//
//    @HandleBeforeDelete
//    public void handleDelete(Account account) {
//
//
//        // Nullify كل الشكاوى المرتبطة بالحساب
//        List<Complaint> complaints = complaintRepo.findByAddedBy(account);
//        for (Complaint c : complaints) {
//            c.setAddedBy(null);
//        }

// add delete logic for rating/voting/complaint
//        Account deletedAccount = accountService.getDeletedAccountPlaceholder();
//
//        // إعادة تعيين account في Ratings
//        ratingRepository.findAllByAccount(account).forEach(r -> {
//            r.setAccount(deletedAccount);
//            ratingRepository.save(r);
//        });
//
//        // إعادة تعيين account في Votings
//        votingRepository.findAllByAccount(account).forEach(v -> {
//            v.setAccount(deletedAccount);
//            votingRepository.save(v);
//        });
//
//        // إذا كانت هناك شكاوى مرتبطة بالحساب
//        complaintRepo.findAllByAddedBy(account).forEach(c -> {
//            c.setAddedBy(deletedAccount);
//            complaintRepo.save(c);
//        });

    }

