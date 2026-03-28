package com.myapp.complaints.DAO;

import com.myapp.complaints.dto.ComplaintImageDto;
import com.myapp.complaints.entity.ComplaintImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintImageRepo extends JpaRepository<ComplaintImage,Long> {

    List<ComplaintImageDto> findByComplaint_Id(Long complaintId);

}
