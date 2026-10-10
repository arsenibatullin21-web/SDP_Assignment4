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

class ParkingFacadeTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void completesParking() {
        GarageTestDsl garage = new GarageTestDsl(temporaryDirectory);

        garage.parkSuccessfully("OP-1");

        garage.checkOperationStatus("OP-1", OperationStatus.COMPLETED);
        assertEquals(EntryStatus.ADMITTED,
                garage.entryService.findEntry("OP-1").getStatus());
        assertEquals(ReservationStatus.RESERVED,
                garage.parkingService.findReservation("OP-1").getStatus());
        assertEquals(PaymentStatus.PAID,
                garage.paymentService.findPayment("OP-1").getStatus());
    }

    @Test
    void cancelsEntryWhenSubsystemBFails() {
        GarageTestDsl garage = new GarageTestDsl(temporaryDirectory);

        garage.failInSubsystemB("OP-B-FAIL");

        garage.checkOperationStatus("OP-B-FAIL", OperationStatus.COMPENSATED);
        assertEquals(EntryStatus.CANCELLED,
                garage.entryService.findEntry("OP-B-FAIL").getStatus());
        assertNull(garage.parkingRepository.findByOperationId("OP-B-FAIL"));
        assertNull(garage.paymentRepository.findByOperationId("OP-B-FAIL"));
    }

    @Test
    void cancelsReservationAndEntryWhenSubsystemCFails() {
        GarageTestDsl garage = new GarageTestDsl(temporaryDirectory);

        garage.failInSubsystemC("OP-C-FAIL");

        garage.checkOperationStatus("OP-C-FAIL", OperationStatus.COMPENSATED);
        assertEquals(ReservationStatus.CANCELLED,
                garage.parkingService.findReservation("OP-C-FAIL").getStatus());
        assertEquals(EntryStatus.CANCELLED,
                garage.entryService.findEntry("OP-C-FAIL").getStatus());
        assertEquals(PaymentStatus.DECLINED,
                garage.paymentService.findPayment("OP-C-FAIL").getStatus());
    }

    @Test
    void rejectsInvalidLicensePlate() {
        GarageTestDsl garage = new GarageTestDsl(temporaryDirectory);

        assertThrows(GarageException.class, () -> garage.parkingFacade.startParking(
                "OP-BAD", "!", "P-01", 2, 1000, FailurePoint.NONE));

        garage.checkOperationStatus("OP-BAD", OperationStatus.FAILED);
        assertNull(garage.entryRepository.findByOperationId("OP-BAD"));
        assertNull(garage.parkingRepository.findByOperationId("OP-BAD"));
        assertNull(garage.paymentRepository.findByOperationId("OP-BAD"));
    }



}
