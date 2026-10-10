package kz.edu.parking.entry;

import kz.edu.parking.common.GarageException;

public class EntryService {
    private final EntryRepository repository;
    private final LicensePlateValidator validator;

    public EntryService(EntryRepository repository, LicensePlateValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public VehicleEntry admitVehicle(String operationId, String licensePlate) {
        validator.validate(licensePlate);

        VehicleEntry entry = new VehicleEntry(operationId, licensePlate, EntryStatus.REGISTERED);
        repository.save(entry);
        entry.admit();
        repository.save(entry);
        return entry;
    }

    public void cancelEntry(String operationId) {
        VehicleEntry entry =
                repository.findByOperationId(operationId);

        if (entry == null) {
            throw new GarageException(
                    "Entry not found: " + operationId
            );
        }

        entry.cancel();
        repository.save(entry);
    }

    public VehicleEntry findEntry(String operationId) {
        VehicleEntry entry =
                repository.findByOperationId(operationId);

        if (entry == null) {
            throw new GarageException(
                    "Entry not found: " + operationId
            );
        }

        return entry;
    }

}
