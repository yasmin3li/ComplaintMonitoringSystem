package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Voting;
import com.myapp.complaints.enums.VotingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VotingRepo extends JpaRepository<Voting,Long> {
    List<Voting> findAllByAccount(Account account);

    Long countByComplaintIdAndType(Long complaintId, VotingType votingType);


    Optional<Voting>  findByAccountIdAndComplaintId(Long accountId, Long complaintId);
}
