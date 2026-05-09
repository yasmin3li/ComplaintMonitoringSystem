package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ComplaintTracingLogRepo extends JpaRepository<ComplaintTrackingLog,Long> {

    List<ComplaintTrackingLog> findByComplaintId(Long complaintId);

    Optional<ComplaintTrackingLog> findTopByComplaint_IdAndActionBy_IdAndNewStateOrderByActionDateDesc(Long complaintId, long accountId, ComplaintState state);

    Optional<ComplaintTrackingLog> findTopByComplaint_IdOrderByActionDateDesc(Long id);

//    @Query("SELECT COUNT(DISTINCT l.complaint.id) FROM ComplaintTrackingLog l WHERE l.actionBy.id = :accountId AND l.actionDate BETWEEN :start AND :end")
//    long countDistinctComplaintByActionByIdAndActionDateBetween(@Param("accountId") Long accountId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

//    @Query("SELECT COUNT(DISTINCT l.complaint.id) FROM ComplaintTrackingLog l WHERE l.assignedTo.account.id = :accountId AND l.actionDate BETWEEN :start AND :end")
//    long countDistinctComplaintAssignedToAccountBetween(@Param("accountId") Long accountId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query("SELECT COUNT( l.complaint.id) FROM ComplaintTrackingLog l WHERE l.assignedTo.account.id = :accountId AND l.actionDate BETWEEN :start AND :end")
    long countComplaintAssignedToAccountBetween(@Param("accountId") Long accountId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
    SELECT COUNT( l.complaint.id)
    FROM ComplaintTrackingLog l
    WHERE l.complaint.institution.id = :instId
      AND l.complaint.governorate.id = :govId
      AND l.newState = com.myapp.complaints.enums.ComplaintState.NEW
      AND l.actionDate BETWEEN :start AND :end
    """)
    long countComplaintsWithNewStateByInstitutionAndGovernorateBetween(
            @Param("instId") Long institutionId,
            @Param("govId") Long governorateId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
