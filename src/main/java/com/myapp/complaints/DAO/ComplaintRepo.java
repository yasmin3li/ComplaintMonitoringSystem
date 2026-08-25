package com.myapp.complaints.DAO;

import com.myapp.complaints.dto.DelayedComplaintsProjection;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.enums.ComplaintPriority;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//Using Specification Query executor
public interface ComplaintRepo extends JpaRepository<Complaint,Long> , JpaSpecificationExecutor<Complaint> {


    long countByDeletedFalse(); // All Complaints

    long countByStateAndDeletedFalse(ComplaintState state);

    long countByDateTimeOfAddBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    long countByAddedBy_EmailAndDeletedFalse(String email);
    long countByStateAndAddedBy_EmailAndDeletedFalse(ComplaintState state, String email);


    Optional<Complaint>  findByIdAndDeletedFalse(Long complaintId);

    @Modifying
    @Query("""
            UPDATE Complaint c
            SET c.state = 'IN_REVIEW'
            WHERE c.id = :id
            AND c.state = 'NEW'
            """)
    int openIfNew(@Param("id") Long id);

    @Query("""
            SELECT COUNT(c)
            FROM Complaint c
            WHERE c.state = :state
            AND c.id IN (
                SELECT l.complaint.id
                FROM ComplaintTrackingLog l
                WHERE l.newState = :state
                AND l.actionBy.id = :employeeId
                AND l.actionDate = (
                    SELECT MAX(l2.actionDate)
                    FROM ComplaintTrackingLog l2
                    WHERE l2.complaint.id = l.complaint.id
                )
            )
            AND c.governorate.id = :govId
            AND c.institution.id = :instId
            AND c.sector.id = :sectorId
            """)
    long countComplaintsByStateForEmployee(
            @Param("state") ComplaintState state,
            @Param("employeeId") Long employeeId,
            @Param("govId") Long govId,
            @Param("instId") Long instId,
            @Param("sectorId") Long sectorId
    );

    long countByStateAndGovernorate_IdAndInstitution_IdAndSector_Id(ComplaintState complaintState, Long id, Long id1, Long id2);

    @Query("""
        SELECT COUNT(c.id)
        FROM Complaint c
        WHERE c.institution.id = :instId
        AND c.governorate.id = :govId
        AND c.dateTimeOfAdd BETWEEN :start AND :end
        """)
    long countCreatedComplaintsBetween(
            @Param("instId") Long institutionId,
            @Param("govId") Long governorateId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT COUNT(c)
    FROM Complaint c
    WHERE c.deleted = false
    AND c.assignedTo.account.id = :accountId
    AND c.state = ComplaintState.ASSIGNED
""")
    long countAssignedComplaints(
            @Param("accountId") Long accountId
    );

    @Query("""
    SELECT COUNT(DISTINCT l.complaint.id)
    FROM ComplaintTrackingLog l
    WHERE l.assignedTo.account.id = :accountId
      AND l.actionType = com.myapp.complaints.enums.ActionType.ASSIGNED
      AND l.actionDate >= :start
      AND l.actionDate < :end
""")
    long countAssignedComplaintsBetween(
            @Param("accountId") Long accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT COUNT(c)
    FROM Complaint c
    WHERE c.deleted = false
    AND c.assignedTo.account.id = :accountId
    AND c.state = com.myapp.complaints.enums.ComplaintState.IN_PROGRESS
""")
    long countInProgressComplaints(
            @Param("accountId") Long accountId
    );

    @Query("""
        SELECT
            c.id as complaintId,
            c.title as title,
            c.priority as priority,
            c.assignedAt as assignedAt,
            c.state as state
        FROM Complaint c
        JOIN c.logs l
        WHERE c.deleted = false
          AND l.assignedTo.account.id = :accountId
          AND c.assignedTo.account.id =:accountId
          AND c.state IN (
              com.myapp.complaints.enums.ComplaintState.ASSIGNED,
              com.myapp.complaints.enums.ComplaintState.IN_PROGRESS
          )
        GROUP BY
            c.id,
            c.title,
            c.priority,
            c.state
""")
    List<DelayedComplaintsProjection> delayedComplaints(
            @Param("accountId") Long accountId);

    List<Complaint> findByPriorityAndStateAndGovernorateIdAndInstitutionId(
            ComplaintPriority complaintPriority, ComplaintState state,
            Long governorateId, Long institutionId
            );


//Complaints Dynamic Query Filter
//    @Query("""
//        SELECT c FROM Complaint c
//        WHERE c.deleted = false
//        AND (:governorate_Id IS NULL OR c.governorate_Id = :governorateId)
//        AND (:sector_Id IS NULL OR c.sector_Id = :sectorId)
//        AND (:institution_Id IS NULL OR c.institution_Id = :institutionId)
//        AND (:state IS NULL OR c.state = :state)
//        AND (:email IS NULL OR c.addedBy.email = :email)
//        ORDER BY c.dateTimeOfAdd DESC
//        """)
//    List<Complaint> filterComplaints(
//            Long governorateId,
//            Long sectorId,
//            Long institutionId,
//            ComplaintState state,
//            String email,
//            Pageable pageable
//    );

}
