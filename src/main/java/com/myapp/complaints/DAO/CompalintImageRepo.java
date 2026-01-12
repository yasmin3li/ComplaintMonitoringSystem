package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.ComplaintImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompalintImageRepo extends JpaRepository<ComplaintImage,Long> {
}
