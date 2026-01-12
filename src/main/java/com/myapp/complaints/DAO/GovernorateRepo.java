package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Governorate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface GovernorateRepo extends JpaRepository<Governorate,Long> {
}
