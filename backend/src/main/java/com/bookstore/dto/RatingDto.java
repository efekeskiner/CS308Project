package com.bookstore.dto;

public class RatingDto {
    private final Double averageRating;
    private final Long ratingCount;

    public RatingDto(Double averageRating, Long ratingCount) {
        this.averageRating = averageRating;
        this.ratingCount = ratingCount != null ? ratingCount : 0L;
    }

    public Double getAverageRating() { return averageRating; }
    public Long getRatingCount() { return ratingCount; }
}
