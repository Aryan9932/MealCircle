package com.mealcircle2.mealcircle2.controller;

import com.mealcircle2.mealcircle2.dto.RazorpayOrderResponse;
import com.mealcircle2.mealcircle2.dto.RazorpayPaymentVerifyRequest;
import com.mealcircle2.mealcircle2.model.Mess;
import com.mealcircle2.mealcircle2.service.RazorpayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payment/razorpay")
public class PaymentController {

  private final RazorpayService razorpayService;

  public PaymentController(RazorpayService razorpayService) {
    this.razorpayService = razorpayService;
  }

  @PostMapping("/order/{messId}")
  public ResponseEntity<?> createOrder(@PathVariable String messId, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    try {
      RazorpayOrderResponse response = razorpayService.createOrder(messId, authentication.getName());
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/verify-and-join")
  public ResponseEntity<?> verifyAndJoin(@RequestBody RazorpayPaymentVerifyRequest request,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    try {
      Mess joinedMess = razorpayService.verifyPaymentAndJoin(request, authentication.getName());
      return ResponseEntity.ok(Map.of(
          "message", "Payment verified and mess joined successfully",
          "paymentId", request.getRazorpayPaymentId(),
          "mess", joinedMess));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
