package kz.edu.parking.payment;

import kz.edu.parking.common.GarageException;

public class Payment {
    private final String operationId;
    private final int amount;
    private PaymentStatus status;

    public Payment(String operationId, int amount, PaymentStatus status) {
        this.operationId = operationId;
        this.amount = amount;
        this.status = status;
    }

    public void markPaid() {
        if (status != PaymentStatus.CREATED) {
            throw new GarageException("Only a new payment can be completed");
        }
        status = PaymentStatus.PAID;
    }

    public void decline() {
        if (status != PaymentStatus.CREATED) {
            throw new GarageException("Only a new payment can be declined");
        }
        status = PaymentStatus.DECLINED;
    }

    public void refund() {
        if (status == PaymentStatus.REFUNDED) {
            return;
        }
        if (status != PaymentStatus.PAID) {
            throw new GarageException("Only a paid payment can be refunded");
        }
        status = PaymentStatus.REFUNDED;
    }

    public String getOperationId() {
        return operationId;
    }

    public int getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
