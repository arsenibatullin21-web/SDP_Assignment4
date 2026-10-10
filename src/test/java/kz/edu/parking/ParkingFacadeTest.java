package kz.edu.parking;

import kz.edu.parking.common.GarageException;
import kz.edu.parking.entry.EntryStatus;
import kz.edu.parking.facade.FailurePoint;
import kz.edu.parking.facade.OperationStatus;
import kz.edu.parking.facade.ParkingOperation;
import kz.edu.parking.payment.PaymentStatus;
import kz.edu.parking.space.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkingFacadeTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void completesTheThreeStageChain() {
        TestGarage garage = new TestGarage(temporaryDirectory);
        GarageTestDsl scenario = new GarageTestDsl(garage);

        scenario.whenDriverParksSuccessfully("OP-1");
        scenario.thenOperationIs("OP-1", OperationStatus.COMPLETED);

        assertEquals(EntryStatus.ADMITTED,
                garage.entryService.findEntry("OP-1").getStatus());
        assertEquals(ReservationStatus.RESERVED,
                garage.parkingService.findReservation("OP-1").getStatus());
        assertEquals(PaymentStatus.PAID,
                garage.paymentService.findPayment("OP-1").getStatus());
    }

    @Test
    void compensatesOnlyAWhenBFailAfterACommitted() {
        TestGarage garage = new TestGarage(temporaryDirectory);
        GarageTestDsl scenario = new GarageTestDsl(garage);

        scenario.whenSubsystemBFails("OP-B-FAIL");
        scenario.thenOperationIs("OP-B-FAIL", OperationStatus.COMPENSATED);

        assertEquals(EntryStatus.CANCELLED,
                garage.entryService.findEntry("OP-B-FAIL").getStatus());
        assertNull(garage.parkingRepository.findByOperationId("OP-B-FAIL"));
        assertNull(garage.paymentRepository.findByOperationId("OP-B-FAIL"));
    }

    @Test
    void compensatesBThenAWhenCFailsAfterBothCommitted() {
        TestGarage garage = new TestGarage(temporaryDirectory);
        GarageTestDsl scenario = new GarageTestDsl(garage);

        scenario.whenSubsystemCFails("OP-C-FAIL");
        scenario.thenOperationIs("OP-C-FAIL", OperationStatus.COMPENSATED);

        assertEquals(ReservationStatus.CANCELLED,
                garage.parkingService.findReservation("OP-C-FAIL").getStatus());
        assertEquals(EntryStatus.CANCELLED,
                garage.entryService.findEntry("OP-C-FAIL").getStatus());
        assertEquals(PaymentStatus.DECLINED,
                garage.paymentService.findPayment("OP-C-FAIL").getStatus());

    }

    @Test
    void handlesInvalidInputBeforeAnythingCommits() {
        TestGarage garage = new TestGarage(temporaryDirectory);

        assertThrows(GarageException.class, () -> garage.parkingFacade.startParking(
                "OP-BAD", "!", "P-01", 2, 1000, FailurePoint.NONE));

        assertEquals(OperationStatus.FAILED,
                garage.parkingFacade.findOperation("OP-BAD").getStatus());
        assertNull(garage.entryRepository.findByOperationId("OP-BAD"));
        assertNull(garage.parkingRepository.findByOperationId("OP-BAD"));
        assertNull(garage.paymentRepository.findByOperationId("OP-BAD"));
    }

    @Test
    void reloadsPersistedProgressAndContinuesAfterRestart() {
        TestGarage firstApplicationRun = new TestGarage(temporaryDirectory);
        ParkingOperation interrupted = new ParkingOperation(
                "OP-RESTART", "123ABC01", "P-02", 3, 1500,
                OperationStatus.STARTED, "Operation started");
        firstApplicationRun.operationRepository.save(interrupted);
        firstApplicationRun.entryService.admitVehicle("OP-RESTART", "123ABC01");
        interrupted.moveToPendingB();
        firstApplicationRun.operationRepository.save(interrupted);

        TestGarage restartedApplication = new TestGarage(temporaryDirectory);
        ParkingOperation result = restartedApplication.parkingFacade.resume(
                "OP-RESTART", FailurePoint.NONE);

        assertEquals(OperationStatus.COMPLETED, result.getStatus());
        assertEquals(ReservationStatus.RESERVED,
                restartedApplication.parkingService.findReservation("OP-RESTART").getStatus());
        assertEquals(PaymentStatus.PAID,
                restartedApplication.paymentService.findPayment("OP-RESTART").getStatus());
    }

    @Test
    void rejectsDuplicateOperationAndOccupiedSpace() {
        TestGarage garage = new TestGarage(temporaryDirectory);
        GarageTestDsl scenario = new GarageTestDsl(garage);
        scenario.whenDriverParksSuccessfully("OP-FIRST");

        assertThrows(GarageException.class, () -> garage.parkingFacade.startParking(
                "OP-FIRST", "456DEF02", "P-02", 1, 500, FailurePoint.NONE));
        assertThrows(GarageException.class, () -> garage.parkingFacade.startParking(
                "OP-SECOND", "456DEF02", "P-01", 1, 500, FailurePoint.NONE));

        assertEquals(OperationStatus.COMPENSATED,
                garage.parkingFacade.findOperation("OP-SECOND").getStatus());
        assertEquals(EntryStatus.CANCELLED,
                garage.entryService.findEntry("OP-SECOND").getStatus());
    }
}
