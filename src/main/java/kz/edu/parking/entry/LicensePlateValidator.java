package kz.edu.parking.entry;

import kz.edu.parking.common.GarageException;

public class LicensePlateValidator {
    public void validate(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new GarageException("License plate cannot be empty");
        }

        String value = licensePlate.replace(" ", "");
        if (value.length() < 5 || value.length() > 10) {
            throw new GarageException("License plate must contain 5 to 10 characters");
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isLetterOrDigit(value.charAt(i))) {
                throw new GarageException("License plate can contain only letters and digits");
            }
        }
    }
}
