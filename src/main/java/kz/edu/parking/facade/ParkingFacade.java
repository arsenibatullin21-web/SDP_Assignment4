package kz.edu.parking.facade;

import kz.edu.parking.common.GarageException;
import kz.edu.parking.entry.EntryService;
import kz.edu.parking.payment.PaymentService;
import kz.edu.parking.space.ParkingReservation;
import kz.edu.parking.space.ParkingService;

public class ParkingFacade {
    private final EntryService entryService;
    private final ParkingService parkingService;
    private final PaymentService paymentService;
    private final OperationRepository operationRepository;

    public ParkingFacade(EntryService entryService, ParkingService parkingService,
                         PaymentService paymentService, OperationRepository operationRepository) {
        this.entryService = entryService;
        this.parkingService = parkingService;
        this.paymentService = paymentService;
        this.operationRepository = operationRepository;
    }

    public ParkingOperation startParking(String operationId, String licensePlate, String spaceId,
                                         int hours, int paidAmount, FailurePoint failurePoint) {
        if (operationRepository.findById(operationId) != null) {
            throw new GarageException("Operation already exists: " + operationId);
        }

        ParkingOperation operation = new ParkingOperation(
                operationId, licensePlate, spaceId, hours, paidAmount,
                OperationStatus.STARTED, "Operation started");
        operationRepository.save(operation);
        return resume(operationId, failurePoint);
    }

    public ParkingOperation resume(String operationId, FailurePoint failurePoint) {
        ParkingOperation operation = findOperation(operationId);

        if (operation.getStatus() == OperationStatus.COMPLETED
                || operation.getStatus() == OperationStatus.COMPENSATED
                || operation.getStatus() == OperationStatus.FAILED) {
            throw new GarageException("Operation cannot be resumed from " + operation.getStatus());
        }

        try {
            if (operation.getStatus() == OperationStatus.STARTED) {
                entryService.admitVehicle(operationId, operation.getLicensePlate());
                operation.moveToPendingB();
                operationRepository.save(operation);
            }

            operation = findOperation(operationId);
            if (operation.getStatus() == OperationStatus.PENDING_B) {
                boolean fail = failurePoint == FailurePoint.SUBSYSTEM_B;
                parkingService.reserveSpace(operationId, operation.getSpaceId(), operation.getHours(), fail);
                operation.moveToPendingC();
                operationRepository.save(operation);
            }

            operation = findOperation(operationId);
            if (operation.getStatus() == OperationStatus.PENDING_C) {
                ParkingReservation reservation = parkingService.findReservation(operationId);
                boolean fail = failurePoint == FailurePoint.SUBSYSTEM_C;
                paymentService.processPayment(
                        operationId, reservation.getFee(), operation.getPaidAmount(), fail);
                operation.complete();
                operationRepository.save(operation);
            }

            return findOperation(operationId);
        } catch (GarageException exception) {
            compensateAfterFailure(operationId, exception.getMessage());
            throw exception;
        }
    }

    public ParkingOperation findOperation(String operationId) {
        ParkingOperation operation = operationRepository.findById(operationId);

        if (operation == null) {
            throw new GarageException("Operation not found: " + operationId);
        }

        return operation;
    }

    private void compensateAfterFailure(String operationId, String reason) {
        ParkingOperation persistedOperation = findOperation(operationId);

        if (persistedOperation.getStatus() == OperationStatus.PENDING_C) {
            parkingService.cancelReservation(operationId);
            entryService.cancelEntry(operationId);
            persistedOperation.compensate("Payment failed: " + reason);
        } else if (persistedOperation.getStatus() == OperationStatus.PENDING_B) {
            entryService.cancelEntry(operationId);
            persistedOperation.compensate("Space reservation failed: " + reason);
        } else {
            persistedOperation.fail("Entry failed: " + reason);
        }

        operationRepository.save(persistedOperation);
    }
}
