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
    private final OperationHistoryRepository historyRepository;

    public ParkingFacade(EntryService entryService, ParkingService parkingService,
                         PaymentService paymentService, OperationRepository operationRepository,
                         OperationHistoryRepository historyRepository) {
        this.entryService = entryService;
        this.parkingService = parkingService;
        this.paymentService = paymentService;
        this.operationRepository = operationRepository;
        this.historyRepository = historyRepository;
    }

    public ParkingOperation startParking(String operationId, String licensePlate, String spaceId,
                                         int hours, int paidAmount, FailurePoint failurePoint) {
        if (operationRepository.findById(operationId).isPresent()) {
            throw new GarageException("Operation already exists: " + operationId);
        }

        ParkingOperation operation = new ParkingOperation(
                operationId, licensePlate, spaceId, hours, paidAmount,
                OperationStatus.STARTED, "Operation started");
        operationRepository.save(operation);
        historyRepository.add(operationId, "OPERATION_STARTED");
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
                historyRepository.add(operationId, "ENTRY_ADMITTED");
                operation.moveToPendingB();
                operationRepository.save(operation);
            }

            operation = findOperation(operationId);
            if (operation.getStatus() == OperationStatus.PENDING_B) {
                boolean fail = failurePoint == FailurePoint.SUBSYSTEM_B;
                parkingService.reserveSpace(operationId, operation.getSpaceId(), operation.getHours(), fail);
                historyRepository.add(operationId, "SPACE_RESERVED");
                operation.moveToPendingC();
                operationRepository.save(operation);
            }

            operation = findOperation(operationId);
            if (operation.getStatus() == OperationStatus.PENDING_C) {
                ParkingReservation reservation = parkingService.findReservation(operationId);
                boolean fail = failurePoint == FailurePoint.SUBSYSTEM_C;
                paymentService.processPayment(
                        operationId, reservation.getFee(), operation.getPaidAmount(), fail);
                historyRepository.add(operationId, "PAYMENT_COMPLETED");
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
        return operationRepository.findById(operationId)
                .orElseThrow(() -> new GarageException("Operation not found: " + operationId));
    }

    private void compensateAfterFailure(String operationId, String reason) {
        ParkingOperation persistedOperation = findOperation(operationId);

        if (persistedOperation.getStatus() == OperationStatus.PENDING_C) {
            parkingService.cancelReservation(operationId);
            historyRepository.add(operationId, "RESERVATION_CANCELLED");
            entryService.cancelEntry(operationId);
            historyRepository.add(operationId, "ENTRY_CANCELLED");
            persistedOperation.compensate("Payment failed: " + reason);
        } else if (persistedOperation.getStatus() == OperationStatus.PENDING_B) {
            entryService.cancelEntry(operationId);
            historyRepository.add(operationId, "ENTRY_CANCELLED");
            persistedOperation.compensate("Space reservation failed: " + reason);
        } else {
            persistedOperation.fail("Entry failed: " + reason);
        }

        operationRepository.save(persistedOperation);
    }
}
