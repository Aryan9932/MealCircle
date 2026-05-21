package com.mealcircle2.mealcircle2.controller;

import com.mealcircle2.mealcircle2.dto.MessReviewRequest;
import com.mealcircle2.mealcircle2.service.MessReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mess")
public class MessReviewController {

  private final MessReviewService messReviewService;

  public MessReviewController(MessReviewService messReviewService) {
    this.messReviewService = messReviewService;
  }

  @PostMapping("/{messId}/reviews")
  public ResponseEntity<?> addReview(
      @PathVariable String messId,
      @RequestBody MessReviewRequest request,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    if (request.getRating() == null) {
      return ResponseEntity.badRequest().body("rating is required");
    }

    try {
      String customerId = authentication.getName();
      return ResponseEntity.ok(
          messReviewService.addOrUpdateReview(messId, customerId, request.getRating(), request.getComment()));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/{messId}/reviews")
  public ResponseEntity<?> getReviews(@PathVariable String messId) {
    return ResponseEntity.ok(messReviewService.getReviewsByMess(messId));
  }

  @GetMapping("/{messId}/rating")
  public ResponseEntity<?> getAverageRating(@PathVariable String messId) {
    double avg = messReviewService.getAverageRatingForMess(messId);
    long count = messReviewService.getReviewCountForMess(messId);

    return ResponseEntity.ok(Map.of(
        "messId", messId,
        "averageRating", avg,
        "totalReviews", count));
  }

  @GetMapping("/customer/best-rated")
  public ResponseEntity<?> getBestRatedMesses(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    return ResponseEntity.ok(messReviewService.getMessesByBestRating());
  }
}
