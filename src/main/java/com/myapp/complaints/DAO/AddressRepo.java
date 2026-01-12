package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepo extends JpaRepository<Address,Long> {
}
