package com.constructioncompany.messaging;

import com.constructioncompany.api.PaymentRequest;
import com.constructioncompany.config.MessagingProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;

    public PaymentMessagePublisher(RabbitTemplate rabbitTemplate, MessagingProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publishPaymentRequest(PaymentRequest request) {
        rabbitTemplate.convertAndSend(
            properties.getExchange(),
            properties.getPaymentRequestRoutingKey(),
            request
        );
    }
}
