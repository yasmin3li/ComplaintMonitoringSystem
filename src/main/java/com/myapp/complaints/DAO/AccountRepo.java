package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface AccountRepo extends JpaRepository<Account,Long> {

    // denied accounts/search/findByEmail  only at HTTP (close only the endpoint)
   // @RestResource(exported = false)
    Optional<Account> findByEmail(String email);

    @RestResource(path = "active", rel = "active")
    List<Account> findByDeletedFalse();


    Optional<Account> findByUserName(String userName);

    Optional<Account> findByPhoneNumber(String phoneNumber);
}
