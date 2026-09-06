package com.mealcircle2.mealcircle2.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealcircle2.mealcircle2.dto.AttendanceEmailEvent;
import com.mealcircle2.mealcircle2.dto.SubscriptionEmailEvent;
import com.mealcircle2.mealcircle2.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitMQMessagingTest {

    private RabbitTemplate rabbitTemplate;
    private SubscriptionEventProducer producer;
    private EmailService emailService;
    private SubscriptionEmailConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        producer = new SubscriptionEventProducer(rabbitTemplate);
        ReflectionTestUtils.setField(producer, "exchange", "mealcircle.exchange");
        ReflectionTestUtils.setField(producer, "subscriptionRoutingKey", "subscription.created");
        ReflectionTestUtils.setField(producer, "attendanceRoutingKey", "attendance.updated");

        emailService = mock(EmailService.class);
        consumer = new SubscriptionEmailConsumer(emailService);

        objectMapper = new ObjectMapper();
    }

    @Test
    void testProducerPublishSubscriptionEvent() {
        SubscriptionEmailEvent event = SubscriptionEmailEvent.builder()
                .customerEmail("customer@test.com")
                .messName("Royal Mess")
                .joiningDate("2026-08-01")
                .endingDate("2026-08-31")
                .build();

        producer.publishSubscriptionEvent(event);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("mealcircle.exchange"),
                eq("subscription.created"),
                eq(event)
        );
    }

    @Test
    void testProducerPublishAttendanceEvent() {
        AttendanceEmailEvent event = AttendanceEmailEvent.builder()
                .customerEmail("customer@test.com")
                .messName("Royal Mess")
                .date("2026-08-01")
                .type("ABSENT")
                .build();

        producer.publishAttendanceEvent(event);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("mealcircle.exchange"),
                eq("attendance.updated"),
                eq(event)
        );
    }

    @Test
    void testConsumerHandleSubscriptionEvent() {
        SubscriptionEmailEvent event = SubscriptionEmailEvent.builder()
                .customerEmail("customer@test.com")
                .messName("Royal Mess")
                .joiningDate("2026-08-01")
                .endingDate("2026-08-31")
                .build();

        consumer.handleSubscriptionEvent(event);

        verify(emailService, times(1)).sendWelcomeEmail(
                "customer@test.com",
                "Royal Mess",
                "2026-08-01",
                "2026-08-31"
        );
    }

    @Test
    void testConsumerHandleAbsentAttendanceEvent() {
        AttendanceEmailEvent event = AttendanceEmailEvent.builder()
                .customerEmail("customer@test.com")
                .messName("Royal Mess")
                .date("2026-08-01")
                .type("ABSENT")
                .build();

        consumer.handleAttendanceEvent(event);

        verify(emailService, times(1)).sendAbsentEmail(
                "customer@test.com",
                "Royal Mess",
                "2026-08-01"
        );
    }

    @Test
    void testConsumerHandlePresentAttendanceEvent() {
        AttendanceEmailEvent event = AttendanceEmailEvent.builder()
                .customerEmail("customer@test.com")
                .messName("Royal Mess")
                .date("2026-08-01")
                .type("PRESENT")
                .build();

        consumer.handleAttendanceEvent(event);

        verify(emailService, times(1)).sendPresentEmail(
                "customer@test.com",
                "Royal Mess",
                "2026-08-01"
        );
    }

    @Test
    void testEventJsonSerialization() throws Exception {
        SubscriptionEmailEvent subEvent = SubscriptionEmailEvent.builder()
                .customerEmail("customer@test.com")
                .messName("Royal Mess")
                .joiningDate("2026-08-01")
                .endingDate("2026-08-31")
                .build();

        String subJson = objectMapper.writeValueAsString(subEvent);
        SubscriptionEmailEvent deserializedSubEvent = objectMapper.readValue(subJson, SubscriptionEmailEvent.class);
        assertEquals(subEvent.getCustomerEmail(), deserializedSubEvent.getCustomerEmail());
        assertEquals(subEvent.getMessName(), deserializedSubEvent.getMessName());

        AttendanceEmailEvent attEvent = AttendanceEmailEvent.builder()
                .customerEmail("customer@test.com")
                .messName("Royal Mess")
                .date("2026-08-01")
                .type("ABSENT")
                .build();

        String attJson = objectMapper.writeValueAsString(attEvent);
        AttendanceEmailEvent deserializedAttEvent = objectMapper.readValue(attJson, AttendanceEmailEvent.class);
        assertEquals(attEvent.getType(), deserializedAttEvent.getType());
        assertEquals(attEvent.getDate(), deserializedAttEvent.getDate());
    }
}
