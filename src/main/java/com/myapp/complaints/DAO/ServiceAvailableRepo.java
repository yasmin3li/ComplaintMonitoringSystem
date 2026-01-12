package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.ServiceAvailable;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface ServiceAvailableRepo extends JpaRepository<ServiceAvailable,Long> {
    List<ServiceAvailable>
    findByInstitutionId(
            @Param("institutionId") Long institutionId
    );
}
//GET /serviceAvailables/search/findByInstitutionIdAndIsActiveTrue?institutionId=3