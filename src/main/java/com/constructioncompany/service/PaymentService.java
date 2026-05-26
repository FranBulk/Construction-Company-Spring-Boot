package com.constructioncompany.service;

import com.constructioncompany.api.EmployeePaymentStatus;
import com.constructioncompany.api.PaymentRequest;
import com.constructioncompany.api.PaymentResult;
import com.constructioncompany.repository.PaymentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<EmployeePaymentStatus> employeesToPay(int year, int weekNumber) {
        return paymentRepository.findEmployeesToPay(year, weekNumber);
    }

    public Optional<PaymentResult> findPayment(int year, int weekNumber, String employeeId) {
        return paymentRepository.findPayment(new PaymentRequest(year, weekNumber, employeeId));
    }

    public PaymentResult registerPayment(PaymentRequest request) {
        return paymentRepository.registerPayment(request);
    }
}
