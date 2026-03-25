package com.myapp.complaints.DAO;

import com.myapp.complaints.dto.ComplaintTrackingLogDto;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintTracingLogRepo extends JpaRepository<ComplaintTrackingLog,Long> {

    List<ComplaintTrackingLogDto> findByComplaintId(Long complaintId);
}
