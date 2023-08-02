package com.kusher.kusher_in_korea.reviewfeedback.service;

import com.kusher.kusher_in_korea.auth.domain.User;
import com.kusher.kusher_in_korea.auth.repository.UserRepository;
import com.kusher.kusher_in_korea.reviewfeedback.domain.Review;
import com.kusher.kusher_in_korea.reviewfeedback.dto.CreateReviewDto;
import com.kusher.kusher_in_korea.reviewfeedback.dto.ReviewDto;
import com.kusher.kusher_in_korea.reviewfeedback.repository.ReviewRepository;
import com.kusher.kusher_in_korea.tabling.domain.Restaurant;
import com.kusher.kusher_in_korea.tabling.repository.RestaurantRepository;
import com.kusher.kusher_in_korea.util.exception.CustomException;
import com.kusher.kusher_in_korea.util.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    // 리뷰 생성
    public Long createReview(CreateReviewDto createReviewDto) {
        User user = userRepository.findById(createReviewDto.getUserId())
                .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));
        Restaurant restaurant = restaurantRepository.findById(createReviewDto.getRestaurantId())
                .orElseThrow(() -> new CustomException(ResponseCode.RESTAURANT_NOT_FOUND));
        Review review = Review.createReview(user, restaurant, createReviewDto);
        reviewRepository.save(review);
        return review.getId();
    }

    // 특정 식당에 대한 리뷰 조회
    public List<ReviewDto> getReviewsByRestaurantId(Long restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurantId(restaurantId);

        return reviews.stream()
                .map(ReviewDto::new)
                .collect(Collectors.toList());
    }

    // 특정 유저가 남긴 평가 조회
    public List<ReviewDto> getReviewsByUserId(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);

        return reviews.stream()
                .map(ReviewDto::new)
                .collect(Collectors.toList());
    }

    // 리뷰 삭제
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

}
