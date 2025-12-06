package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface AccountRepo extends JpaRepository<Account,Long> {
}
