package com.constructioncompany.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.messaging")
public class MessagingProperties {

    private String exchange;
    private String paymentRequestQueue;
    private String paymentRequestRoutingKey;
    private String paymentResultQueue;
    private String paymentResultRoutingKey;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getPaymentRequestQueue() {
        return paymentRequestQueue;
    }

    public void setPaymentRequestQueue(String paymentRequestQueue) {
        this.paymentRequestQueue = paymentRequestQueue;
    }

    public String getPaymentRequestRoutingKey() {
        return paymentRequestRoutingKey;
    }

    public void setPaymentRequestRoutingKey(String paymentRequestRoutingKey) {
        this.paymentRequestRoutingKey = paymentRequestRoutingKey;
    }

    public String getPaymentResultQueue() {
        return paymentResultQueue;
    }

    public void setPaymentResultQueue(String paymentResultQueue) {
        this.paymentResultQueue = paymentResultQueue;
    }

    public String getPaymentResultRoutingKey() {
        return paymentResultRoutingKey;
    }

    public void setPaymentResultRoutingKey(String paymentResultRoutingKey) {
        this.paymentResultRoutingKey = paymentResultRoutingKey;
    }
}
