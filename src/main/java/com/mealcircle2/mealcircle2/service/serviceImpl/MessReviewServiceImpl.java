package com.mealcircle2.mealcircle2.service.serviceImpl;

import com.mealcircle2.mealcircle2.dto.MessRatingResponse;
import com.mealcircle2.mealcircle2.model.Mess;
import com.mealcircle2.mealcircle2.model.MessReview;
import com.mealcircle2.mealcircle2.repository.MessRepository;
import com.mealcircle2.mealcircle2.repository.MessReviewRepository;
import com.mealcircle2.mealcircle2.repository.SubscriptionRepository;
import com.mealcircle2.mealcircle2.service.MessReviewService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MessReviewServiceImpl implements MessReviewService {

  private final MessReviewRepository messReviewRepository;
  private final MessRepository messRepository;
  private final SubscriptionRepository subscriptionRepository;

  public MessReviewServiceImpl(MessReviewRepository messReviewRepository, MessRepository messRepository,
      SubscriptionRepository subscriptionRepository) {
    this.messReviewRepository = messReviewRepository;
    this.messRepository = messRepository;
    this.subscriptionRepository = subscriptionRepository;
  }

  @Override
  public MessReview addOrUpdateReview(String messId, String customerId, int rating, String comment) {
    if (rating < 1 || rating > 5) {
      throw new RuntimeException("Rating must be between 1 and 5");
    }

    Mess mess = messRepository.findById(messId)
        .orElseThrow(() -> new RuntimeException("Mess not found"));

    // Check if customer is subscribed to this mess
    if (subscriptionRepository.findByCustomerIdAndMessId(customerId, messId).isEmpty()) {
      throw new RuntimeException("Customer is not assigned to this mess");
    }

    Instant now = Instant.now();

    MessReview review = messReviewRepository.findByMessIdAndCustomerId(messId, customerId)
        .orElse(MessReview.builder()
            .messId(messId)
            .customerId(customerId)
            .createdAt(now)
            .build());

    review.setRating(rating);
    review.setComment(comment);
    review.setUpdatedAt(now);

    return messReviewRepository.save(review);
  }

  @Override
  public List<MessReview> getReviewsByMess(String messId) {
    return messReviewRepository.findByMessIdOrderByCreatedAtDesc(messId);
  }

  @Override
  public double getAverageRatingForMess(String messId) {
    List<MessReview> reviews = getReviewsByMess(messId);
    if (reviews.isEmpty()) {
      return 0.0;
    }

    double sum = reviews.stream().mapToInt(MessReview::getRating).sum();
    return sum / reviews.size();
  }

  @Override
  public long getReviewCountForMess(String messId) {
    return messReviewRepository.countByMessId(messId);
  }

  @Override
  public List<MessRatingResponse> getMessesByBestRating() {
    List<Mess> messes = messRepository.findAll();

    return messes.stream()
        .map(mess -> {
          List<MessReview> reviews = messReviewRepository.findByMessIdOrderByCreatedAtDesc(mess.getId());
          long totalReviews = reviews.size();
          double averageRating = totalReviews == 0
              ? 0.0
              : reviews.stream().mapToInt(MessReview::getRating).average().orElse(0.0);

          return MessRatingResponse.builder()
              .mess(mess)
              .averageRating(averageRating)
              .totalReviews(totalReviews)
              .build();
        })
        .sorted(
            Comparator.comparingDouble(MessRatingResponse::getAverageRating).reversed()
                .thenComparing(Comparator.comparingLong(MessRatingResponse::getTotalReviews).reversed()))
        .toList();
  }
}
