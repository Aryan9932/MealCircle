package com.mealcircle2.mealcircle2.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${mealcircle.rabbitmq.queue.subscription}")
    private String subscriptionQueue;

    @Value("${mealcircle.rabbitmq.queue.attendance}")
    private String attendanceQueue;

    @Value("${mealcircle.rabbitmq.exchange}")
    private String exchange;

    @Value("${mealcircle.rabbitmq.routing-key.subscription}")
    private String subscriptionRoutingKey;

    @Value("${mealcircle.rabbitmq.routing-key.attendance}")
    private String attendanceRoutingKey;

    // ── Queues ────────────────────────────────────────────────────────────────

    @Bean
    public Queue subscriptionEmailQueue() {
        // durable=true: queue survives broker restarts
        return new Queue(subscriptionQueue, true);
    }

    @Bean
    public Queue attendanceEmailQueue() {
        return new Queue(attendanceQueue, true);
    }

    // ── Exchange ──────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange mealCircleExchange() {
        return new TopicExchange(exchange);
    }

    // ── Bindings ──────────────────────────────────────────────────────────────

    @Bean
    public Binding subscriptionBinding() {
        return BindingBuilder
                .bind(subscriptionEmailQueue())
                .to(mealCircleExchange())
                .with(subscriptionRoutingKey);
    }

    @Bean
    public Binding attendanceBinding() {
        return BindingBuilder
                .bind(attendanceEmailQueue())
                .to(mealCircleExchange())
                .with(attendanceRoutingKey);
    }

    // ── Message Converter (JSON) ───────────────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
