package kz.edu.parking.payment;

import kz.edu.parking.common.GarageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentRepository {
    private final Path file;

    public PaymentRepository(Path dataDirectory) {
        this.file = dataDirectory.resolve("payments.csv");
        prepareFile();
    }

    public void save(Payment payment) {
        List<Payment> payments = findAll();
        payments.removeIf(saved -> saved.getOperationId().equals(payment.getOperationId()));
        payments.add(payment);

        List<String> lines = new ArrayList<>();
        for (Payment saved : payments) {
            lines.add(saved.getOperationId() + ";" + saved.getAmount() + ";" + saved.getStatus());
        }
        write(lines);
    }

    public Optional<Payment> findByOperationId(String operationId) {
        for (Payment payment : findAll()) {
            if (payment.getOperationId().equals(operationId)) {
                return Optional.of(payment);
            }
        }
        return Optional.empty();
    }

    public List<Payment> findAll() {
        try {
            List<Payment> result = new ArrayList<>();
            for (String line : Files.readAllLines(file)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(";", -1);
                result.add(new Payment(parts[0], Integer.parseInt(parts[1]),
                        PaymentStatus.valueOf(parts[2])));
            }
            return result;
        } catch (IOException exception) {
            throw new GarageException("Cannot read payment data", exception);
        }
    }

    private void prepareFile() {
        try {
            Files.createDirectories(file.getParent());
            if (Files.notExists(file)) {
                Files.createFile(file);
            }
        } catch (IOException exception) {
            throw new GarageException("Cannot create payment storage", exception);
        }
    }

    private void write(List<String> lines) {
        try {
            Files.write(file, lines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new GarageException("Cannot save payment data", exception);
        }
    }
}
