package com.conectea.backend.controller;

import com.conectea.backend.dto.ReviewDTO;
import com.conectea.backend.mappers.ReviewMapper;
import com.conectea.backend.model.Review;
import com.conectea.backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;

    @PostMapping("/institution/{institutionId}")
    public ResponseEntity<ReviewDTO> createReview(
            @PathVariable Long institutionId,
            @RequestBody ReviewDTO reviewDTO,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        
        Review review = reviewService.addReviewToInstitution(
            institutionId, 
            reviewDTO, 
            userEmail
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewMapper.toDTO(review));
    }

    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByInstitution(@PathVariable Long institutionId) {
        List<Review> reviews = reviewService.findByInstitution(institutionId);
        List<ReviewDTO> reviewDTOs = reviews.stream()
                .map(ReviewMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reviewDTOs);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        reviewService.deleteReview(reviewId, userEmail);
        return ResponseEntity.ok("Comentário excluído");
    }
}