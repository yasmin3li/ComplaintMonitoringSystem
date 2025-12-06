package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepo extends JpaRepository<Sector,Long> {
}
