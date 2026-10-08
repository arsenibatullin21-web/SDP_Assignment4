package kz.edu.parking;

import kz.edu.parking.common.GarageException;
import kz.edu.parking.facade.FailurePoint;
import kz.edu.parking.facade.OperationStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GarageTestDsl {
    private final TestGarage garage;

    GarageTestDsl(TestGarage garage) {
        this.garage = garage;
    }

    void whenDriverParksSuccessfully(String operationId) {
        garage.parkingFacade.startParking(
                operationId, "123ABC01", "P-01", 2, 1000, FailurePoint.NONE);
    }

    void whenSubsystemBFails(String operationId) {
        assertThrows(GarageException.class, () -> garage.parkingFacade.startParking(
                operationId, "123ABC01", "P-01", 2, 1000, FailurePoint.SUBSYSTEM_B));
    }

    void whenSubsystemCFails(String operationId) {
        assertThrows(GarageException.class, () -> garage.parkingFacade.startParking(
                operationId, "123ABC01", "P-01", 2, 1000, FailurePoint.SUBSYSTEM_C));
    }

    void thenOperationIs(String operationId, OperationStatus expectedStatus) {
        assertEquals(expectedStatus,
                garage.parkingFacade.findOperation(operationId).getStatus());
    }
}
