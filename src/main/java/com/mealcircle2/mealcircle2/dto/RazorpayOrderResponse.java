package com.mealcircle2.mealcircle2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderResponse {
  private String key;
  private String orderId;
  private int amount;
  private String currency;
  private String messId;
  private String messName;
}
