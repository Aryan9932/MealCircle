package com.mealcircle2.mealcircle2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionResponse {
    private String id;
    private String customerId;
    private String messId;
    private String joiningDate;
    private LocalDate messEndingDate;
    private List<LocalDate> absentDates;
    private int buffer;
    private List<LocalDate> presentDates;
    private double moneyLeftToPay;
}
