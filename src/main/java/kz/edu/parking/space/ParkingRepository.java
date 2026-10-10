package kz.edu.parking.space;

import kz.edu.parking.common.GarageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ParkingRepository {
    private final Path file;

    public ParkingRepository(Path dataDirectory) {
        this.file = dataDirectory.resolve("reservations.csv");
        prepareFile();
    }

    public void save(ParkingReservation reservation) {
        List<ParkingReservation> reservations = findAll();
        boolean reservationWasUpdated = false;

        for (int i = 0; i < reservations.size(); i++) {
            ParkingReservation savedReservation = reservations.get(i);
            if (savedReservation.getOperationId().equals(reservation.getOperationId())) {
                reservations.set(i, reservation);
                reservationWasUpdated = true;
                break;
            }
        }

        if (!reservationWasUpdated) {
            reservations.add(reservation);
        }

        List<String> lines = new ArrayList<>();
        for (ParkingReservation saved : reservations) {
            lines.add(saved.getOperationId() + ";" + saved.getSpaceId() + ";"
                    + saved.getHours() + ";" + saved.getFee() + ";" + saved.getStatus());
        }
        write(lines);
    }

    public ParkingReservation findByOperationId(String operationId) {
        for (ParkingReservation reservation : findAll()) {
            if (reservation.getOperationId().equals(operationId)) {
                return reservation;
            }
        }
        return null;
    }

    public boolean isSpaceReserved(String spaceId) {
        for (ParkingReservation reservation : findAll()) {
            if (reservation.getSpaceId().equalsIgnoreCase(spaceId)
                    && reservation.getStatus() == ReservationStatus.RESERVED) {
                return true;
            }
        }
        return false;
    }

    public List<ParkingReservation> findAll() {
        try {
            List<ParkingReservation> result = new ArrayList<>();
            for (String line : Files.readAllLines(file)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(";", -1);
                result.add(new ParkingReservation(
                        parts[0], parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
                        ReservationStatus.valueOf(parts[4])));
            }
            return result;
        } catch (IOException exception) {
            throw new GarageException("Cannot read reservation data", exception);
        }
    }

    private void prepareFile() {
        try {
            Files.createDirectories(file.getParent());
            if (Files.notExists(file)) {
                Files.createFile(file);
            }
        } catch (IOException exception) {
            throw new GarageException("Cannot create reservation storage", exception);
        }
    }

    private void write(List<String> lines) {
        try {
            Files.write(file, lines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new GarageException("Cannot save reservation data", exception);
        }
    }
}
