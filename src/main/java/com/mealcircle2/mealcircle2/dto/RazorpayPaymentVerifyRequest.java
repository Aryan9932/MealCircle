package com.mealcircle2.mealcircle2.dto;

import lombok.Data;

@Data
public class RazorpayPaymentVerifyRequest {
  private String messId;

  private String razorpayOrderId;

  private String razorpayPaymentId;

  private String razorpaySignature;
}
