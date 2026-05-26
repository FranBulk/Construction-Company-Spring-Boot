package com.constructioncompany.web;

import com.constructioncompany.api.EmployeePaymentStatus;
import com.constructioncompany.api.PaymentRequest;
import com.constructioncompany.api.PaymentResult;
import com.constructioncompany.messaging.PaymentMessagePublisher;
import com.constructioncompany.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMessagePublisher paymentMessagePublisher;

    public PaymentController(PaymentService paymentService, PaymentMessagePublisher paymentMessagePublisher) {
        this.paymentService = paymentService;
        this.paymentMessagePublisher = paymentMessagePublisher;
    }

    @GetMapping("/due")
    public List<EmployeePaymentStatus> employeesToPay(
        @RequestParam @Min(2000) @Max(9999) int year,
        @RequestParam @Min(1) @Max(53) int weekNumber
    ) {
        return paymentService.employeesToPay(year, weekNumber);
    }

    @GetMapping("/{year}/{weekNumber}/{employeeId}")
    public ResponseEntity<PaymentResult> paymentByPeriod(
        @PathVariable @Min(2000) @Max(9999) int year,
        @PathVariable @Min(1) @Max(53) int weekNumber,
        @PathVariable @NotBlank String employeeId
    ) {
        return paymentService.findPayment(year, weekNumber, employeeId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> requestPayment(@Valid @RequestBody PaymentRequest request) {
        paymentMessagePublisher.publishPaymentRequest(request);
        return ResponseEntity.accepted().body(Map.of(
            "status", "QUEUED",
            "message", "Payment request was sent to RabbitMQ and will be processed by Apache Camel.",
            "request", request
        ));
    }

    @PostMapping("/pay-now")
    public PaymentResult payImmediately(@Valid @RequestBody PaymentRequest request) {
        return paymentService.registerPayment(request);
    }
}
