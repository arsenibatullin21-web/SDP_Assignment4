package kz.edu.parking.facade;

import kz.edu.parking.common.GarageException;
import kz.edu.parking.entry.EntryService;
import kz.edu.parking.payment.PaymentService;
import kz.edu.parking.space.ParkingService;

import java.util.List;

public class ParkingAdminFacade {
    private final EntryService entryService;
    private final ParkingService parkingService;
    private final PaymentService paymentService;
    private final OperationRepository operationRepository;

    public ParkingAdminFacade(EntryService entryService, ParkingService parkingService,
                              PaymentService paymentService, OperationRepository operationRepository) {
        this.entryService = entryService;
        this.parkingService = parkingService;
        this.paymentService = paymentService;
        this.operationRepository = operationRepository;
    }

    public String inspect(String operationId) {
        ParkingOperation operation = findOperation(operationId);
        return "Operation " + operation.getId()
                + " | plate=" + operation.getLicensePlate()
                + " | space=" + operation.getSpaceId()
                + " | status=" + operation.getStatus()
                + " | message=" + operation.getMessage();
    }

    public List<ParkingOperation> findAllOperations() {
        return operationRepository.findAll();
    }

    public ParkingOperation compensateManually(String operationId) {
        ParkingOperation operation = findOperation(operationId);

        if (operation.getStatus() == OperationStatus.COMPENSATED) {
            return operation;
        }

        if (operation.getStatus() == OperationStatus.COMPLETED) {
            paymentService.refundPayment(operationId);
            parkingService.cancelReservation(operationId);
            entryService.cancelEntry(operationId);
        } else if (operation.getStatus() == OperationStatus.PENDING_C) {
            parkingService.cancelReservation(operationId);
            entryService.cancelEntry(operationId);
        } else if (operation.getStatus() == OperationStatus.PENDING_B) {
            entryService.cancelEntry(operationId);
        } else if (operation.getStatus() != OperationStatus.STARTED
                && operation.getStatus() != OperationStatus.FAILED) {
            throw new GarageException("Operation cannot be compensated from " + operation.getStatus());
        }

        operation.compensate("Manual compensation completed");
        operationRepository.save(operation);
        return operation;
    }

    private ParkingOperation findOperation(String operationId) {
        ParkingOperation operation = operationRepository.findById(operationId);

        if (operation == null) {
            throw new GarageException("Operation not found: " + operationId);
        }

        return operation;
    }
}
