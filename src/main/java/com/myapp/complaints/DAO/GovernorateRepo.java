package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Governorate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GovernorateRepo extends JpaRepository<Governorate,Long> {
}
