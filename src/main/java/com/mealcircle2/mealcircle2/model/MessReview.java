package com.mealcircle2.mealcircle2.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "mess_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessReview {

  @Id
  private String id;

  private String messId;
  private String customerId;

  private int rating;
  private String comment;

  private Instant createdAt;
  private Instant updatedAt;
}
