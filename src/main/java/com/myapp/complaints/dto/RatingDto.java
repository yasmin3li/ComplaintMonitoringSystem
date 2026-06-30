package com.myapp.complaints.dto;

public record RatingDto(
        Boolean IsRating,
        Integer stareNumber,
        Double rate
) {
}
