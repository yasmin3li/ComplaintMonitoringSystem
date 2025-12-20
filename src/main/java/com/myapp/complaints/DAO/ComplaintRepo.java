package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepo extends JpaRepository<Complaint,Long> {
//    List<Complaint> findByAddedBy(Account account);
//
//    List <Complaint> findAllByAddedBy(Account account);
}
