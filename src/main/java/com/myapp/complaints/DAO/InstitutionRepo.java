package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepo extends JpaRepository<Institution,Long> {
}
