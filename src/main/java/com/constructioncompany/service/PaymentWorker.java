package com.constructioncompany.service;

import com.constructioncompany.api.PaymentRequest;
import com.constructioncompany.api.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentWorker.class);

    private final PaymentService paymentService;

    public PaymentWorker(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public PaymentResult process(PaymentRequest request) {
        LOGGER.info(
            "Processing payroll payment request employee={} year={} week={}",
            request.employeeId(),
            request.year(),
            request.weekNumber()
        );
        return paymentService.registerPayment(request);
    }
}
