package kz.edu.parking.entry;

import kz.edu.parking.common.GarageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntryRepository {
    private final Path file;

    public EntryRepository(Path dataDirectory) {
        this.file = dataDirectory.resolve("entries.csv");
        prepareFile();
    }

    public void save(VehicleEntry entry) {
        List<VehicleEntry> entries = findAll();
        entries.removeIf(saved -> saved.getOperationId().equals(entry.getOperationId()));
        entries.add(entry);

        List<String> lines = new ArrayList<>();
        for (VehicleEntry saved : entries) {
            lines.add(saved.getOperationId() + ";" + saved.getLicensePlate() + ";" + saved.getStatus());
        }
        write(lines);
    }

    public Optional<VehicleEntry> findByOperationId(String operationId) {
        for (VehicleEntry entry : findAll()) {
            if (entry.getOperationId().equals(operationId)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public List<VehicleEntry> findAll() {
        try {
            List<VehicleEntry> result = new ArrayList<>();
            for (String line : Files.readAllLines(file)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(";", -1);
                result.add(new VehicleEntry(parts[0], parts[1], EntryStatus.valueOf(parts[2])));
            }
            return result;
        } catch (IOException exception) {
            throw new GarageException("Cannot read entry data", exception);
        }
    }

    private void prepareFile() {
        try {
            Files.createDirectories(file.getParent());
            if (Files.notExists(file)) {
                Files.createFile(file);
            }
        } catch (IOException exception) {
            throw new GarageException("Cannot create entry storage", exception);
        }
    }

    private void write(List<String> lines) {
        try {
            Files.write(file, lines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new GarageException("Cannot save entry data", exception);
        }
    }
}
