package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.InstitutionSectorGovernorate;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface InstitutionSectorGovernorateRepo extends JpaRepository<InstitutionSectorGovernorate,Long> {
    List<InstitutionSectorGovernorate>
    findBySectorGovernorateIdAndIsActiveTrue(@Param("sectorGovernorateId") Long sectorGovernorateId);
}
//GET /institutionSectorGovernorates/search/findBySectorGovernorateId?sectorGovernorateId=5