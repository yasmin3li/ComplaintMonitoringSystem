package com.myapp.complaints.DAO;

import com.myapp.complaints.dto.ComplaintTrackingLogDto;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintTracingLogRepo extends JpaRepository<ComplaintTrackingLog,Long> {

    List<ComplaintTrackingLogDto> findByComplaintId(Long complaintId);

    Optional<ComplaintTrackingLog> findByComplaint_IdAndActionBy_IdAndNewState(Long complaintId, long accountId, ComplaintState state);

    List<ComplaintTrackingLog> findByComplaint_IdAndActionBy_Id(Long id, Long id1);
}
