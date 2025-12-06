package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepo extends JpaRepository<Complaint,Long> {
}
