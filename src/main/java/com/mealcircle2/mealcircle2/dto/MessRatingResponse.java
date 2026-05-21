package com.mealcircle2.mealcircle2.dto;

import com.mealcircle2.mealcircle2.model.Mess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessRatingResponse {

  private Mess mess;
  private double averageRating;
  private long totalReviews;
}
