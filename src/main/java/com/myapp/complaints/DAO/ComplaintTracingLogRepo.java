package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.ComplaintTrackingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintTracingLogRepo extends JpaRepository<ComplaintTrackingLog,Long> {
}
