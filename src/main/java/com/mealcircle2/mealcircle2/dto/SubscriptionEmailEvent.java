package com.mealcircle2.mealcircle2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Message published to RabbitMQ when a customer successfully subscribes to a mess.
 * Consumed by {@code SubscriptionEmailConsumer} to send the welcome/confirmation email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionEmailEvent implements Serializable {

    private String customerEmail;
    private String messName;
    private String joiningDate;
    private String endingDate;
}
