package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
//@RepositoryRestResource(
//        excerptProjection = ComplaintSummaryProjection.class
//)
public interface ComplaintRepo extends JpaRepository<Complaint,Long> {

    @Query("select c from Complaint c where c.deleted = false")

    List<Complaint> findTop10ByDeletedFalseOrderByDateTimeOfAddDesc();
    long countByDeletedFalse(); // All Complaints
    List<Complaint> findBySectorIdAndDeletedFalse(Long sectorId);
    List<Complaint> findByInstitutionIdAndDeletedFalse(Long institutionId);
    List<Complaint> findByGovernorateIdAndDeletedFalse(Long governorateId);
    List<Complaint> findByStateAndDeletedFalse(ComplaintState state); //filter
    long countByStateAndDeletedFalse(ComplaintState state);
//
//    @Query("""
//        SELECT COUNT(c)
//        FROM Complaint c
//        WHERE c.dateTimeOfAdd >= :startOfDay
//        AND c.deleted = false
//        """)
//    long countTodayComplaints(LocalDate startOfDay);

    long countByDateTimeOfAddBetween(
            LocalDateTime start,
            LocalDateTime end
    );

}
