package com.constructioncompany.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.constructioncompany.api.PaymentRequest;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.stereotype.Component;

@Component
public class PaymentRoute extends RouteBuilder {

    private final ObjectMapper objectMapper;

    public PaymentRoute(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure() {
        JacksonDataFormat paymentRequestFormat = new JacksonDataFormat(PaymentRequest.class);
        paymentRequestFormat.setObjectMapper(objectMapper);

        JacksonDataFormat paymentResultFormat = new JacksonDataFormat();
        paymentResultFormat.setObjectMapper(objectMapper);

        from("spring-rabbitmq:{{app.messaging.exchange}}"
            + "?queues={{app.messaging.payment-request-queue}}"
            + "&routingKey={{app.messaging.payment-request-routing-key}}"
            + "&autoDeclare=false")
            .routeId("payment-request-consumer")
            .unmarshal(paymentRequestFormat)
            .bean("paymentWorker", "process")
            .marshal(paymentResultFormat)
            .to("spring-rabbitmq:{{app.messaging.exchange}}"
                + "?routingKey={{app.messaging.payment-result-routing-key}}");
    }
}
