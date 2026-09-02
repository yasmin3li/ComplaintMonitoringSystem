package com.myapp.complaints.DAO;

import com.myapp.complaints.dto.ComplaintResponseProjection;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ComplaintTracingLogRepo extends JpaRepository<ComplaintTrackingLog,Long> {

    List<ComplaintTrackingLog> findByComplaintId(Long complaintId);

    Optional<ComplaintTrackingLog> findTopByComplaint_IdAndActionBy_IdAndNewStateOrderByActionDateDesc(Long complaintId, long accountId, ComplaintState state);

    Optional<ComplaintTrackingLog> findTopByComplaint_IdOrderByActionDateDesc(Long id);

    @Query("SELECT COUNT(DISTINCT l.complaint.id) FROM ComplaintTrackingLog l WHERE l.assignedTo.account.id = :accountId AND l.actionDate BETWEEN :start AND :end")
    long countDistinctComplaintAssignedToAccountBetween(@Param("accountId") Long accountId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    @Query("SELECT COUNT( l.complaint.id) FROM ComplaintTrackingLog l WHERE l.assignedTo.account.id = :id")
    long countComplaintAssignedToAccount(Long id);

    @Query("SELECT Min( l.actionDate) FROM ComplaintTrackingLog l WHERE l.assignedTo.account.id = :accountId")
    LocalDateTime findFirstHandledByEmp(Long accountId);
//
//    @Query("SELECT COUNT( l.complaint.id) " +
//            "FROM ComplaintTrackingLog l " +
//            "WHERE l.assignedTo.account.id = :accountId " +
//            "AND l.newState = :state " +
//            "AND l.actionDate BETWEEN :start AND :end")
//    long countComplaintAssignedToAccountBetweenWithState(
//            @Param("accountId") Long accountId,
//            @Param("state") ComplaintState state,
//            @Param("start") LocalDateTime start,
//            @Param("end") LocalDateTime end);

    @Query("""
    SELECT COUNT(DISTINCT l.complaint.id)
    FROM ComplaintTrackingLog l
    WHERE l.actionBy.id = :accountId
    AND l.newState IN (
        ComplaintState.FORWARDED_TO_MANAGER,
        ComplaintState.REJECTED
    )
    AND l.actionDate BETWEEN :start AND :end
    """)
    long countHandledComplaintsBetween(
            @Param("accountId") Long accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    @Query("""
        SELECT MIN(l.actionDate)
        FROM ComplaintTrackingLog l
        WHERE l.complaint.id = :complaintId
        AND l.actionBy.id = :accountId
        """)
    LocalDateTime findFirstActionTime(
            @Param("complaintId") Long complaintId,
            @Param("accountId") Long accountId
    );

    @Query("""
        SELECT
            c.dateTimeOfAdd AS createdAt,
            MIN(l.actionDate) AS reviewedAt
        FROM ComplaintTrackingLog l
        JOIN l.complaint c
        WHERE l.assignedTo.account.id = :accountId
          AND l.newState = :state
          AND l.actionDate BETWEEN :start AND :end
        GROUP BY c.id, c.dateTimeOfAdd
    """)
    List<ComplaintResponseProjection> findComplaintResponseTimes(
            @Param("accountId") Long accountId,
            @Param("state") ComplaintState state,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    Optional<ComplaintTrackingLog> findTopByComplaint_IdAndComplaint_AddedBy_IdAndNewStateOrderByActionDateDesc(Long id, Long id1, ComplaintState complaintState);

    @Query("""
    SELECT COUNT(DISTINCT l.complaint.id)
    FROM ComplaintTrackingLog l
    WHERE l.actionBy.id = :accountId
    AND l.newState =ComplaintState.RESOLVED
    AND l.actionDate BETWEEN :start AND :end
""")
    long countResolvedComplaintsBetween(
            @Param("accountId") Long accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    Optional<ComplaintTrackingLog> findTopByComplaint_IdAndNewStateOrderByActionDateDesc(Long complaintId, ComplaintState state);


    @Query("SELECT DISTINCT l.complaint.id FROM" +
            " ComplaintTrackingLog l" +
            " WHERE" +
            " l.assignedTo.account.id = :accountId" +
            " AND l.actionDate BETWEEN :start AND :end ")
    List<Long> findDistinctComplaintIdsAssignedToAccountBetween(
            @Param("accountId") Long accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT MIN(l.actionDate)" +
            " FROM ComplaintTrackingLog l" +
            " WHERE l.complaint.id = :complaintId" +
            " AND l.actionType = com.myapp.complaints.enums.ActionType.ASSIGNED" +
            " AND l.assignedTo.account.id = :accountId")
    LocalDateTime findAssignedAt(@Param("complaintId") Long complaintId, @Param("accountId") Long accountId);

    @Query("SELECT MIN(l.actionDate) FROM" +
            " ComplaintTrackingLog l WHERE" +
            " l.complaint.id = :complaintId AND" +
            " l.actionType = com.myapp.complaints.enums.ActionType.STARTED" +
            " AND l.assignedTo.account.id = :accountId")
    LocalDateTime findStartedAt(@Param("complaintId") Long complaintId, @Param("accountId") Long accountId);

    @Query("""
    SELECT COUNT(DISTINCT l.complaint.id)
    FROM ComplaintTrackingLog l
    WHERE l.newState =ComplaintState.FORWARDED_TO_MANAGER
    AND l.actionDate BETWEEN :start AND :end
    AND l.complaint.governorate.id =:governorateId
    AND l.complaint.institution.id =:institutionId
""")
    long countIncomingComplaintsBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("governorateId") Long governorateId,
            @Param("institutionId") Long institutionId
    );

    @Query("""
    SELECT COUNT(DISTINCT l.complaint.id)
    FROM ComplaintTrackingLog l
    WHERE l.actionBy.id = :accountId
    AND l.newState =ComplaintState.RESOLVED
""")
    long countResolvedComplaints(
            @Param("accountId") Long accountId
    );

    @Query("""
    SELECT l.actionBy
    FROM ComplaintTrackingLog l
    WHERE l.complaint.id = :complaintId
    AND l.assignedTo.account.id = :accountId
    AND l.actionType = com.myapp.complaints.enums.ActionType.ASSIGNED
    ORDER BY l.actionDate DESC
""")
    Account findAssignedByForComplaint(
            @Param("complaintId") Long complaintId,
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

//    @Query("""
//    SELECT COUNT(DISTINCT l.complaint.id)
//    FROM ComplaintTrackingLog l
//    WHERE l.assignedTo.account.id = :accountId
//      AND l.actionType = com.myapp.complaints.enums.ActionType.ASSIGNED
//      AND l.actionDate >= :start
//      AND l.actionDate < :end
//""")
//    long countInProgressComplaintsBetween(
//            @Param("accountId") Long accountId,
//            @Param("start") LocalDateTime start,
//            @Param("end") LocalDateTime end
//    );

    @Query("""
            SELECT COUNT(c)
            FROM ComplaintTrackingLog c
            WHERE c.newState = com.myapp.complaints.enums.ComplaintState.FORWARDED_TO_MANAGER
                AND c.actionBy.id = :employeeId
            """)
    long countComplaintsByStateForEmployee(
            @Param("employeeId") Long employeeId
    );
}
