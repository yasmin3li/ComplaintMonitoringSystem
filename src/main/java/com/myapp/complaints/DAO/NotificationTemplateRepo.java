package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.NotificationTemplate;
import com.myapp.complaints.enums.ComplaintState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepo extends JpaRepository<NotificationTemplate,Long> {
    Optional<NotificationTemplate> findByStateAndRoleId(ComplaintState state, Long role);

}
