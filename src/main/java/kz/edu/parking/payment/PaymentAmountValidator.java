package kz.edu.parking.payment;

import kz.edu.parking.common.GarageException;

public class PaymentAmountValidator {
    public void validate(int expectedAmount, int paidAmount) {
        if (paidAmount <= 0) {
            throw new GarageException("Payment amount must be positive");
        }
        if (paidAmount != expectedAmount) {
            throw new GarageException("Expected payment is " + expectedAmount + " KZT");
        }
    }
}
