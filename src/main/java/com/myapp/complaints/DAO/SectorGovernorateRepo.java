package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Governorate;
import com.myapp.complaints.entity.SectorGovernorate;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectorGovernorateRepo extends JpaRepository<SectorGovernorate,Long> {
    List<SectorGovernorate>
    findByGovernorateId(@Param("governorateId") Long governorateId);

//    verification from consistence
    boolean existsBySectorIdAndGovernorateId(Long sectorId, Long governorateId);

    List<SectorGovernorate> findByGovernorate_Id(Long governorateId);
}

//GET /sectorGovernorates/search/findByGovernorateId?governorateId=1

