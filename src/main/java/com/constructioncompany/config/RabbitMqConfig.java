package com.constructioncompany.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    @Bean
    DirectExchange constructionExchange(MessagingProperties properties) {
        return new DirectExchange(properties.getExchange(), true, false);
    }

    @Bean
    Queue paymentRequestQueue(MessagingProperties properties) {
        return new Queue(properties.getPaymentRequestQueue(), true);
    }

    @Bean
    Queue paymentResultQueue(MessagingProperties properties) {
        return new Queue(properties.getPaymentResultQueue(), true);
    }

    @Bean
    Binding paymentRequestBinding(
        DirectExchange constructionExchange,
        Queue paymentRequestQueue,
        MessagingProperties properties
    ) {
        return BindingBuilder.bind(paymentRequestQueue)
            .to(constructionExchange)
            .with(properties.getPaymentRequestRoutingKey());
    }

    @Bean
    Binding paymentResultBinding(
        DirectExchange constructionExchange,
        Queue paymentResultQueue,
        MessagingProperties properties
    ) {
        return BindingBuilder.bind(paymentResultQueue)
            .to(constructionExchange)
            .with(properties.getPaymentResultRoutingKey());
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
