package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Voting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingRepo extends JpaRepository<Voting,Long> {
}
