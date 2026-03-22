package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
//Using Specification Query executor
public interface ComplaintRepo extends JpaRepository<Complaint,Long> , JpaSpecificationExecutor<Complaint> {

//    @Query("select c from Complaint c where c.deleted = false")
//    List<Complaint> findByDeletedFalseOrderByDateTimeOfAddDesc(Pageable pageable);

    long countByDeletedFalse(); // All Complaints
//    List<Complaint> findBySectorIdAndDeletedFalse(Long sectorId);
//    List<Complaint> findByInstitutionIdAndDeletedFalse(Long institutionId);
//    List<Complaint> findByGovernorateIdAndDeletedFalse(Long governorateId);
//    List<Complaint> findByStateAndDeletedFalse(ComplaintState state); //filter
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

    long countByAddedBy_EmailAndDeletedFalse(String email);
    long countByStateAndAddedBy_EmailAndDeletedFalse(ComplaintState state, String email);

//    List<Complaint> findByAddedBy_EmailAndDeletedFalse(String email);
//    List<Complaint> findByAddedBy_Email(String email);
//    List<Complaint> findTop3ByAddedBy_EmailAndDeletedFalse(String email);


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
