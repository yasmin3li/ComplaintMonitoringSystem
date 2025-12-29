package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface RoleRepo extends JpaRepository<Role,Long> {
    Optional<Role> findByName(String name);
//    Role findById(Long id);
}
