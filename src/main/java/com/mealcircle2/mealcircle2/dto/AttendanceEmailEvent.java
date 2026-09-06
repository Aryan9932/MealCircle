package com.mealcircle2.mealcircle2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Message published to RabbitMQ when a customer's attendance is updated (absent or present).
 * Consumed by {@code SubscriptionEmailConsumer} to send the corresponding notification email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEmailEvent implements Serializable {

    private String customerEmail;
    private String messName;
    private String date;

    /**
     * Attendance type: either "ABSENT" or "PRESENT"
     */
    private String type;
}
