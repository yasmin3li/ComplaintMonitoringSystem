package com.myapp.complaints.DAO;

import com.myapp.complaints.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepo extends JpaRepository<Rating,Long> {
}
