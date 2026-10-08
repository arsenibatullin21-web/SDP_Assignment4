package kz.edu.parking.space;

import kz.edu.parking.common.GarageException;

public class ParkingService {
    private final ParkingRepository repository;
    private final ParkingFeeCalculator feeCalculator;

    public ParkingService(ParkingRepository repository, ParkingFeeCalculator feeCalculator) {
        this.repository = repository;
        this.feeCalculator = feeCalculator;
    }

    public ParkingReservation reserveSpace(String operationId, String spaceId, int hours,
                                           boolean simulateFailure) {
        if (simulateFailure) {
            throw new GarageException("The parking sensor reports that the space is unavailable");
        }
        if (spaceId == null || spaceId.isBlank()) {
            throw new GarageException("Parking space cannot be empty");
        }
        if (repository.isSpaceReserved(spaceId)) {
            throw new GarageException("Parking space is already reserved: " + spaceId);
        }

        int fee = feeCalculator.calculate(hours);
        ParkingReservation reservation = new ParkingReservation(
                operationId, spaceId, hours, fee, ReservationStatus.REQUESTED);
        repository.save(reservation);
        reservation.reserve();
        repository.save(reservation);
        return reservation;
    }

    public void cancelReservation(String operationId) {
        ParkingReservation reservation = repository.findByOperationId(operationId)
                .orElseThrow(() -> new GarageException("Reservation not found: " + operationId));
        reservation.cancel();
        repository.save(reservation);
    }

    public ParkingReservation findReservation(String operationId) {
        return repository.findByOperationId(operationId)
                .orElseThrow(() -> new GarageException("Reservation not found: " + operationId));
    }
}
