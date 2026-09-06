package com.mealcircle2.mealcircle2.messaging;

import com.mealcircle2.mealcircle2.dto.AttendanceEmailEvent;
import com.mealcircle2.mealcircle2.dto.SubscriptionEmailEvent;
import com.mealcircle2.mealcircle2.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes messages from RabbitMQ queues and delegates email sending to {@link EmailService}.
 *
 * <p>Two listeners are defined:
 * <ul>
 *   <li>{@code mealcircle.subscription.queue} – sends subscription confirmation (welcome) emails</li>
 *   <li>{@code mealcircle.attendance.queue}   – sends absent / present notification emails</li>
 * </ul>
 */
@Component
public class SubscriptionEmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionEmailConsumer.class);

    private final EmailService emailService;

    public SubscriptionEmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Listens on the subscription queue and sends a welcome/confirmation email
     * when a new subscription event is received.
     */
    @RabbitListener(queues = "${mealcircle.rabbitmq.queue.subscription}")
    public void handleSubscriptionEvent(SubscriptionEmailEvent event) {
        log.info("[RabbitMQ Consumer] Received subscription event for customer: {}, mess: {}",
                event.getCustomerEmail(), event.getMessName());
        try {
            emailService.sendWelcomeEmail(
                    event.getCustomerEmail(),
                    event.getMessName(),
                    event.getJoiningDate(),
                    event.getEndingDate()
            );
            log.info("[RabbitMQ Consumer] Welcome email sent to: {}", event.getCustomerEmail());
        } catch (Exception e) {
            log.error("[RabbitMQ Consumer] Failed to send welcome email to {}: {}",
                    event.getCustomerEmail(), e.getMessage());
        }
    }

    /**
     * Listens on the attendance queue and sends an absent or present notification email
     * based on the event type.
     */
    @RabbitListener(queues = "${mealcircle.rabbitmq.queue.attendance}")
    public void handleAttendanceEvent(AttendanceEmailEvent event) {
        log.info("[RabbitMQ Consumer] Received attendance event ({}) for customer: {}, mess: {}",
                event.getType(), event.getCustomerEmail(), event.getMessName());
        try {
            if ("ABSENT".equalsIgnoreCase(event.getType())) {
                emailService.sendAbsentEmail(
                        event.getCustomerEmail(),
                        event.getMessName(),
                        event.getDate()
                );
                log.info("[RabbitMQ Consumer] Absent email sent to: {}", event.getCustomerEmail());
            } else if ("PRESENT".equalsIgnoreCase(event.getType())) {
                emailService.sendPresentEmail(
                        event.getCustomerEmail(),
                        event.getMessName(),
                        event.getDate()
                );
                log.info("[RabbitMQ Consumer] Present email sent to: {}", event.getCustomerEmail());
            } else {
                log.warn("[RabbitMQ Consumer] Unknown attendance type '{}' — email skipped.", event.getType());
            }
        } catch (Exception e) {
            log.error("[RabbitMQ Consumer] Failed to send attendance email to {}: {}",
                    event.getCustomerEmail(), e.getMessage());
        }
    }
}
