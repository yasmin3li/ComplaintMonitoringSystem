package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.InstitutionGovernorate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface InstitutionGovernorateRepo extends JpaRepository<InstitutionGovernorate,Long> {
}
