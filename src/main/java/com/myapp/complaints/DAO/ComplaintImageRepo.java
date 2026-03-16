package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.ComplaintImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintImageRepo extends JpaRepository<ComplaintImage,Long> {

//    //because the relation with complaint is uni => use this repo for get complaint's images
//    List<ComplaintImage> findByComplaintId(Long complaintId);
}
