package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface InstitutionRepo extends JpaRepository<Institution,Long> {
}
