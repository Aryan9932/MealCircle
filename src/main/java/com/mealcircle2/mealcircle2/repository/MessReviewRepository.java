package com.mealcircle2.mealcircle2.repository;

import com.mealcircle2.mealcircle2.model.MessReview;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MessReviewRepository extends MongoRepository<MessReview, String> {

  Optional<MessReview> findByMessIdAndCustomerId(String messId, String customerId);

  List<MessReview> findByMessIdOrderByCreatedAtDesc(String messId);

  long countByMessId(String messId);
}
