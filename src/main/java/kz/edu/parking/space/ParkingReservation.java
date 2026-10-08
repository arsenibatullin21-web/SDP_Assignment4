package kz.edu.parking.space;

import kz.edu.parking.common.GarageException;

public class ParkingReservation {
    private final String operationId;
    private final String spaceId;
    private final int hours;
    private final int fee;
    private ReservationStatus status;

    public ParkingReservation(String operationId, String spaceId, int hours, int fee,
                              ReservationStatus status) {
        this.operationId = operationId;
        this.spaceId = spaceId;
        this.hours = hours;
        this.fee = fee;
        this.status = status;
    }

    public void reserve() {
        if (status != ReservationStatus.REQUESTED) {
            throw new GarageException("Only a requested space can be reserved");
        }
        status = ReservationStatus.RESERVED;
    }

    public void cancel() {
        if (status == ReservationStatus.CANCELLED) {
            return;
        }
        if (status != ReservationStatus.REQUESTED && status != ReservationStatus.RESERVED) {
            throw new GarageException("The reservation cannot be cancelled");
        }
        status = ReservationStatus.CANCELLED;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public int getHours() {
        return hours;
    }

    public int getFee() {
        return fee;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
