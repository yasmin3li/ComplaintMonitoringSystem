package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComplaintTracingLogRepo extends JpaRepository<ComplaintTrackingLog,Long> {

//    Optional<ComplaintTrackingLog> findByComplaintId(Long complaintId);
}
