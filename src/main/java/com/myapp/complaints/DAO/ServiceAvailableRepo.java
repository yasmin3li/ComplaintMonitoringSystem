package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.ServiceAvailable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ServiceAvailableRepo extends JpaRepository<ServiceAvailable,Long> {
}
