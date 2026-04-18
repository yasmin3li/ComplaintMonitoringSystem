package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.enums.AccountStatus;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface AccountRepo extends JpaRepository<Account,Long> {

    // denied accounts/search/findByEmail  only at HTTP (close only the endpoint)
   // @RestResource(exported = false)
    Optional<Account> findByEmailAndDeletedFalse(String email);

    @RestResource(path = "active", rel = "active")
    List<Account> findByDeletedFalse();


    Optional<Account> findByUserName(String userName);

    Optional<Account> findByPhoneNumberAndDeletedFalse(String phoneNumber);


    @Query("""
        SELECT a
        FROM Account a
        WHERE a.status = :status
        AND a.email = :email
    """)
    Optional<Account> findByEmailAndStatusAndDeletedFalse(
            String email,
            AccountStatus status
    );

    @Query("""
        SELECT a
        FROM Account a
        WHERE a.status = :status
        AND a.phoneNumber = :phoneNumber
    """)
    Optional<Account> findByPhoneNumberAndStatusAndDeletedFalse(
            String phoneNumber,
            AccountStatus status
    );

    Account findByNationalNumberAndDeletedTrue(@NotEmpty(message = "nationalNumber  must not be empty") String s);
}
