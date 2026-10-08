package kz.edu.parking.payment;

import kz.edu.parking.common.GarageException;

public class PaymentService {
    private final PaymentRepository repository;
    private final PaymentAmountValidator validator;

    public PaymentService(PaymentRepository repository, PaymentAmountValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public Payment processPayment(String operationId, int expectedAmount, int paidAmount,
                                  boolean simulateFailure) {
        Payment payment = new Payment(operationId, paidAmount, PaymentStatus.CREATED);
        repository.save(payment);

        try {
            if (simulateFailure) {
                throw new GarageException("The payment terminal is temporarily unavailable");
            }
            validator.validate(expectedAmount, paidAmount);
            payment.markPaid();
            repository.save(payment);
            return payment;
        } catch (GarageException exception) {
            payment.decline();
            repository.save(payment);
            throw exception;
        }
    }

    public void refundPayment(String operationId) {
        Payment payment = repository.findByOperationId(operationId)
                .orElseThrow(() -> new GarageException("Payment not found: " + operationId));
        payment.refund();
        repository.save(payment);
    }

    public Payment findPayment(String operationId) {
        return repository.findByOperationId(operationId)
                .orElseThrow(() -> new GarageException("Payment not found: " + operationId));
    }
}
