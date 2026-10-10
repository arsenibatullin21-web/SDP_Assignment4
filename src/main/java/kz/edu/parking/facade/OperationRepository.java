package kz.edu.parking.facade;

import kz.edu.parking.common.GarageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class OperationRepository {
    private final Path file;

    public OperationRepository(Path dataDirectory) {
        this.file = dataDirectory.resolve("operations.csv");
        prepareFile();
    }

    public void save(ParkingOperation operation) {
        List<ParkingOperation> operations = findAll();
        boolean operationWasUpdated = false;

        for (int i = 0; i < operations.size(); i++) {
            ParkingOperation savedOperation = operations.get(i);
            if (savedOperation.getId().equals(operation.getId())) {
                operations.set(i, operation);
                operationWasUpdated = true;
                break;
            }
        }

        if (!operationWasUpdated) {
            operations.add(operation);
        }

        List<String> lines = new ArrayList<>();
        for (ParkingOperation saved : operations) {
            String safeMessage = saved.getMessage().replace("|", "/");
            lines.add(saved.getId() + "|" + saved.getLicensePlate() + "|" + saved.getSpaceId()
                    + "|" + saved.getHours() + "|" + saved.getPaidAmount() + "|"
                    + saved.getStatus() + "|" + safeMessage);
        }
        write(lines);
    }

    public ParkingOperation findById(String id) {
        for (ParkingOperation operation : findAll()) {
            if (operation.getId().equals(id)) {
                return operation;
            }
        }
        return null;
    }

    public List<ParkingOperation> findAll() {
        try {
            List<ParkingOperation> result = new ArrayList<>();
            for (String line : Files.readAllLines(file)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                result.add(new ParkingOperation(
                        parts[0], parts[1], parts[2], Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4]), OperationStatus.valueOf(parts[5]), parts[6]));
            }
            return result;
        } catch (IOException exception) {
            throw new GarageException("Cannot read operation data", exception);
        }
    }

    private void prepareFile() {
        try {
            Files.createDirectories(file.getParent());
            if (Files.notExists(file)) {
                Files.createFile(file);
            }
        } catch (IOException exception) {
            throw new GarageException("Cannot create operation storage", exception);
        }
    }

    private void write(List<String> lines) {
        try {
            Files.write(file, lines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new GarageException("Cannot save operation data", exception);
        }
    }
}
