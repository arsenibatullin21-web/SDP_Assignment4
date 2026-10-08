package kz.edu.parking.space;

import kz.edu.parking.common.GarageException;

public class ParkingFeeCalculator {
    private static final int PRICE_PER_HOUR = 500;

    public int calculate(int hours) {
        if (hours <= 0 || hours > 24) {
            throw new GarageException("Parking duration must be between 1 and 24 hours");
        }
        return hours * PRICE_PER_HOUR;
    }
}
