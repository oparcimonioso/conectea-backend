package com.conectea.backend.service;

import com.conectea.backend.dto.ReviewDTO;
import com.conectea.backend.model.Institution;
import com.conectea.backend.model.Review;
import com.conectea.backend.model.User;
import com.conectea.backend.repository.InstitutionRepository;
import com.conectea.backend.repository.ReviewRepository;
import com.conectea.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRepository userRepository;

    public Review addReviewToInstitution(Long institutionId, ReviewDTO reviewDTO, String userEmail) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Instituição não encontrada"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Review review = new Review();
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setDate(LocalDateTime.now());
        review.setInstitution(institution);
        review.setUser(user);

        return reviewRepository.save(review);
    }

    public List<Review> findByInstitution(Long institutionId) {
        return reviewRepository.findByInstitutionId(institutionId);
    }

    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        if (!review.getUser().getEmail().equals(userEmail) && !user.isAdmin()) {
            throw new RuntimeException("Não autorizado a excluir esta avaliação");
        }

        reviewRepository.delete(review);
    }
}