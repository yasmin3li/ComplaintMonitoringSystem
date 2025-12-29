package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.projection.ComplaintSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
//@RepositoryRestResource(
//        excerptProjection = ComplaintSummaryProjection.class
//)
public interface ComplaintRepo extends JpaRepository<Complaint,Long> {
//    List<Complaint> findByAddedBy(Account account);
//
    @Query("select c from Complaint c where c.deleted = false")
    List<Complaint> findAllActive();

//    List <Complaint> findAllByAddedBy(Account account);
}
