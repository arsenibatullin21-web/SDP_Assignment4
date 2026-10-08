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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkingAdminFacadeTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void inspectsAndManuallyCompensatesCompletedOperation() {
        TestGarage garage = new TestGarage(temporaryDirectory);
        garage.parkingFacade.startParking(
                "OP-ADMIN", "123ABC01", "P-01", 2, 1000, FailurePoint.NONE);

        String description = garage.adminFacade.inspect("OP-ADMIN");
        ParkingOperation compensated = garage.adminFacade.compensateManually("OP-ADMIN");

        assertTrue(description.contains("status=COMPLETED"));
        assertEquals(OperationStatus.COMPENSATED, compensated.getStatus());
        assertEquals(PaymentStatus.REFUNDED,
                garage.paymentService.findPayment("OP-ADMIN").getStatus());
        assertEquals(ReservationStatus.CANCELLED,
                garage.parkingService.findReservation("OP-ADMIN").getStatus());
        assertEquals(EntryStatus.CANCELLED,
                garage.entryService.findEntry("OP-ADMIN").getStatus());
        assertEquals(1, garage.adminFacade.findAllOperations().size());
        assertEquals(OperationStatus.COMPENSATED,
                garage.adminFacade.compensateManually("OP-ADMIN").getStatus());
    }

    @Test
    void reportsUnknownOperation() {
        TestGarage garage = new TestGarage(temporaryDirectory);

        assertThrows(GarageException.class, () -> garage.adminFacade.inspect("MISSING"));
        assertThrows(GarageException.class,
                () -> garage.adminFacade.compensateManually("MISSING"));
    }
}
