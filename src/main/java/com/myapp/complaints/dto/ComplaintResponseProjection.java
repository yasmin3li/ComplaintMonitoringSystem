package com.myapp.complaints.dto;

import java.time.LocalDateTime;

public interface ComplaintResponseProjection {

    LocalDateTime getCreatedAt();

    LocalDateTime getReviewedAt();
}