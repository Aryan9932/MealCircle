package com.mealcircle2.mealcircle2.service;

import com.mealcircle2.mealcircle2.dto.MessRatingResponse;
import com.mealcircle2.mealcircle2.model.MessReview;

import java.util.List;

public interface MessReviewService {

  MessReview addOrUpdateReview(String messId, String customerId, int rating, String comment);

  List<MessReview> getReviewsByMess(String messId);

  double getAverageRatingForMess(String messId);

  long getReviewCountForMess(String messId);

  List<MessRatingResponse> getMessesByBestRating();
}
