package kz.edu.parking.facade;

import kz.edu.parking.common.GarageException;

public class ParkingOperation {
    private final String id;
    private final String licensePlate;
    private final String spaceId;
    private final int hours;
    private final int paidAmount;
    private OperationStatus status;
    private String message;

    public ParkingOperation(String id, String licensePlate, String spaceId, int hours,
                            int paidAmount, OperationStatus status, String message) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.spaceId = spaceId;
        this.hours = hours;
        this.paidAmount = paidAmount;
        this.status = status;
        this.message = message;
    }

    public void moveToPendingB() {
        requireStatus(OperationStatus.STARTED);
        status = OperationStatus.PENDING_B;
        message = "Entry committed; waiting for a parking space";
    }

    public void moveToPendingC() {
        requireStatus(OperationStatus.PENDING_B);
        status = OperationStatus.PENDING_C;
        message = "Space reserved; waiting for payment";
    }

    public void complete() {
        requireStatus(OperationStatus.PENDING_C);
        status = OperationStatus.COMPLETED;
        message = "Parking operation completed";
    }

    public void compensate(String reason) {
        if (status == OperationStatus.COMPENSATED) {
            return;
        }
        status = OperationStatus.COMPENSATED;
        message = reason;
    }

    public void fail(String reason) {
        status = OperationStatus.FAILED;
        message = reason;
    }

    private void requireStatus(OperationStatus expected) {
        if (status != expected) {
            throw new GarageException("Illegal operation transition from " + status);
        }
    }

    public String getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public int getHours() {
        return hours;
    }

    public int getPaidAmount() {
        return paidAmount;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
