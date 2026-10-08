package kz.edu.parking.facade;

import kz.edu.parking.common.GarageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class OperationHistoryRepository {
    private final Path file;

    public OperationHistoryRepository(Path dataDirectory) {
        this.file = dataDirectory.resolve("history.csv");
        prepareFile();
    }

    public void add(String operationId, String event) {
        String line = operationId + ";" + event + System.lineSeparator();
        try {
            Files.writeString(file, line, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            throw new GarageException("Cannot save operation history", exception);
        }
    }

    public List<String> findEvents(String operationId) {
        try {
            List<String> result = new ArrayList<>();
            for (String line : Files.readAllLines(file)) {
                String[] parts = line.split(";", 2);
                if (parts.length == 2 && parts[0].equals(operationId)) {
                    result.add(parts[1]);
                }
            }
            return result;
        } catch (IOException exception) {
            throw new GarageException("Cannot read operation history", exception);
        }
    }

    private void prepareFile() {
        try {
            Files.createDirectories(file.getParent());
            if (Files.notExists(file)) {
                Files.createFile(file);
            }
        } catch (IOException exception) {
            throw new GarageException("Cannot create history storage", exception);
        }
    }
}
