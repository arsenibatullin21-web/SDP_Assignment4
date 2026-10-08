package kz.edu.parking.demo;

import kz.edu.parking.payment.PaymentAmountValidator;
import kz.edu.parking.payment.PaymentRepository;
import kz.edu.parking.payment.PaymentService;

import java.nio.file.Path;

public class PaymentSubsystemDemo {
    public static void main(String[] args) {
        Path directory = Path.of("demo-data", "payment");
        PaymentService service = new PaymentService(
                new PaymentRepository(directory), new PaymentAmountValidator());
        service.processPayment("PAYMENT-DEMO", 1000, 1000, false);
        System.out.println(service.findPayment("PAYMENT-DEMO").getStatus());
    }
}
