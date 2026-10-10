package kz.edu.parking;

import kz.edu.parking.common.GarageException;
import kz.edu.parking.entry.EntryStatus;
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
    void manuallyCompensatesCompletedOperation() {
        GarageTestDsl garage = new GarageTestDsl(temporaryDirectory);
        garage.parkSuccessfully("OP-ADMIN");

        String description = garage.adminFacade.inspect("OP-ADMIN");
        ParkingOperation result = garage.adminFacade.compensateManually("OP-ADMIN");

        assertTrue(description.contains("status=COMPLETED"));
        assertEquals(OperationStatus.COMPENSATED, result.getStatus());
        assertEquals(PaymentStatus.REFUNDED,
                garage.paymentService.findPayment("OP-ADMIN").getStatus());
        assertEquals(ReservationStatus.CANCELLED,
                garage.parkingService.findReservation("OP-ADMIN").getStatus());
        assertEquals(EntryStatus.CANCELLED,
                garage.entryService.findEntry("OP-ADMIN").getStatus());
    }

    @Test
    void rejectsUnknownOperation() {
        GarageTestDsl garage = new GarageTestDsl(temporaryDirectory);

        assertThrows(GarageException.class,
                () -> garage.adminFacade.inspect("MISSING"));
        assertThrows(GarageException.class,
                () -> garage.adminFacade.compensateManually("MISSING"));
    }
}
