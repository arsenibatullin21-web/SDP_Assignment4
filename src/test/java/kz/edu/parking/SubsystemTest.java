package kz.edu.parking;

import kz.edu.parking.common.GarageException;
import kz.edu.parking.entry.EntryStatus;
import kz.edu.parking.entry.LicensePlateValidator;
import kz.edu.parking.entry.VehicleEntry;
import kz.edu.parking.payment.Payment;
import kz.edu.parking.payment.PaymentAmountValidator;
import kz.edu.parking.payment.PaymentStatus;
import kz.edu.parking.space.ParkingFeeCalculator;
import kz.edu.parking.space.ParkingReservation;
import kz.edu.parking.space.ReservationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubsystemTest {
    @Test
    void validatesLicensePlates() {
        LicensePlateValidator validator = new LicensePlateValidator();
        validator.validate("123 ABC 01");

        assertThrows(GarageException.class, () -> validator.validate(null));
        assertThrows(GarageException.class, () -> validator.validate("123"));
        assertThrows(GarageException.class, () -> validator.validate("12345-ABC"));
    }

    @Test
    void calculatesParkingFee() {
        ParkingFeeCalculator calculator = new ParkingFeeCalculator();
        assertEquals(1500, calculator.calculate(3));
        assertThrows(GarageException.class, () -> calculator.calculate(0));
        assertThrows(GarageException.class, () -> calculator.calculate(25));
    }

    @Test
    void validatesPaymentAmount() {
        PaymentAmountValidator validator = new PaymentAmountValidator();
        validator.validate(1000, 1000);
        assertThrows(GarageException.class, () -> validator.validate(1000, 0));
        assertThrows(GarageException.class, () -> validator.validate(1000, 500));
    }


}
