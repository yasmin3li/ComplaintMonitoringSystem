package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface SectorRepo extends JpaRepository<Sector,Long> {
}
