package com.mealcircle2.mealcircle2.service;

import com.mealcircle2.mealcircle2.dto.RazorpayOrderResponse;
import com.mealcircle2.mealcircle2.dto.RazorpayPaymentVerifyRequest;
import com.mealcircle2.mealcircle2.model.Mess;

public interface RazorpayService {
  RazorpayOrderResponse createOrder(String messId, String customerId);

  Mess verifyPaymentAndJoin(RazorpayPaymentVerifyRequest request, String customerId);
}
