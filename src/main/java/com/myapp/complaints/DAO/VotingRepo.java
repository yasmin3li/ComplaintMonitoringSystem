package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Voting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VotingRepo extends JpaRepository<Voting,Long> {
    List<Voting> findAllByAccount(Account account);
}
