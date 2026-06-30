package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepo extends JpaRepository<Rating,Long> {
    List<Rating> findAllByAccount(Account account);

    Optional<Rating> findByAccountIdAndComplaintId(Long accountId, Long complaintId);

    List<Rating> findByComplaintId(Long complaintId);
}
