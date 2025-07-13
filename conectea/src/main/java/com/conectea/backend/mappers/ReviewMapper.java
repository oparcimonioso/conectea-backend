package com.conectea.backend.mappers;

import com.conectea.backend.dto.ReviewDTO;
import com.conectea.backend.model.Review;
import java.time.format.DateTimeFormatter;

public class ReviewMapper {
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static ReviewDTO toDTO(Review review) {
        if (review == null) return null;

        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());

        if (review.getDate() != null) {
            dto.setDate(review.getDate().format(FORMATTER));
        }

        if (review.getUser() != null) {
            dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
            dto.setUserId(review.getUser().getId());
        }

        return dto;
    }

    public static Review toEntity(ReviewDTO dto) {
        Review review = new Review();
        review.setId(dto.getId());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        return review;
    }
}