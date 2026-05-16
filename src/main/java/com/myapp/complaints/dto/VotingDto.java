package com.myapp.complaints.dto;

import com.myapp.complaints.enums.VotingType;

public record VotingDto(
        long likesNumber,
        long disLikeNumber,
        boolean isLike,
        boolean isDisLike
) {}
