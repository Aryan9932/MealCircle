package com.mealcircle2.mealcircle2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "subscriptions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subscription {
    @Id
    private String id;
    private String customerId;
    private String messId;
    private LocalDateTime joiningDate;
    private LocalDate messEndingDate;
    private List<LocalDate> absentDates;
    @Builder.Default
    private int buffer = 10; // max days allowed to be absent
    private List<LocalDate> presentDates;
    @Builder.Default
    private double moneyLeftToPay = 0.0;
}
