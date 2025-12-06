package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.ServiceAvailable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceAvailableRepo extends JpaRepository<ServiceAvailable,Long> {
}
