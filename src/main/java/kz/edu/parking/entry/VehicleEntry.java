package kz.edu.parking.entry;

import kz.edu.parking.common.GarageException;

public class VehicleEntry {
    private final String operationId;
    private final String licensePlate;
    private EntryStatus status;

    public VehicleEntry(String operationId, String licensePlate, EntryStatus status) {
        this.operationId = operationId;
        this.licensePlate = licensePlate;
        this.status = status;
    }

    public void admit() {
        if (status != EntryStatus.REGISTERED) {
            throw new GarageException("Only a registered vehicle can be admitted");
        }
        status = EntryStatus.ADMITTED;
    }

    public void cancel() {
        if (status == EntryStatus.CANCELLED) {
            return;
        }
        if (status != EntryStatus.REGISTERED && status != EntryStatus.ADMITTED) {
            throw new GarageException("The entry cannot be cancelled");
        }
        status = EntryStatus.CANCELLED;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public EntryStatus getStatus() {
        return status;
    }
}
