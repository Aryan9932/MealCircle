package com.mealcircle2.mealcircle2.messaging;

import com.mealcircle2.mealcircle2.dto.AttendanceEmailEvent;
import com.mealcircle2.mealcircle2.dto.SubscriptionEmailEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publishes subscription and attendance events to RabbitMQ queues via a TopicExchange.
 */
@Component
public class SubscriptionEventProducer {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${mealcircle.rabbitmq.exchange}")
    private String exchange;

    @Value("${mealcircle.rabbitmq.routing-key.subscription}")
    private String subscriptionRoutingKey;

    @Value("${mealcircle.rabbitmq.routing-key.attendance}")
    private String attendanceRoutingKey;

    public SubscriptionEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes a {@link SubscriptionEmailEvent} to the subscription queue.
     * Triggered after a customer successfully joins a mess.
     */
    public void publishSubscriptionEvent(SubscriptionEmailEvent event) {
        log.info("[RabbitMQ] Publishing subscription event for customer: {}, mess: {}",
                event.getCustomerEmail(), event.getMessName());
        rabbitTemplate.convertAndSend(exchange, subscriptionRoutingKey, event);
    }

    /**
     * Publishes an {@link AttendanceEmailEvent} to the attendance queue.
     * Triggered when a customer is marked absent or present.
     */
    public void publishAttendanceEvent(AttendanceEmailEvent event) {
        log.info("[RabbitMQ] Publishing attendance event ({}) for customer: {}, mess: {}",
                event.getType(), event.getCustomerEmail(), event.getMessName());
        rabbitTemplate.convertAndSend(exchange, attendanceRoutingKey, event);
    }
}
