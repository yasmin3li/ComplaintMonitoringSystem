package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface CitizenRepo extends JpaRepository<Citizen,Long> {
}
