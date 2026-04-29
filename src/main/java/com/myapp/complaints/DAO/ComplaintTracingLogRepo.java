package com.myapp.complaints.DAO;

import com.myapp.complaints.dto.ComplaintTrackingLogDto;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintTracingLogRepo extends JpaRepository<ComplaintTrackingLog,Long> {

    List<ComplaintTrackingLog> findByComplaintId(Long complaintId);

    Optional<ComplaintTrackingLog> findByComplaint_IdAndActionBy_IdAndNewState(Long complaintId, long accountId, ComplaintState state);

    Optional<ComplaintTrackingLog> findTopByComplaint_IdOrderByActionDateDesc(Long id);
}
