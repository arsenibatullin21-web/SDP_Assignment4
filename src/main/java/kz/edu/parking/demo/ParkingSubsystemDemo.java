package kz.edu.parking.demo;

import kz.edu.parking.space.ParkingFeeCalculator;
import kz.edu.parking.space.ParkingRepository;
import kz.edu.parking.space.ParkingService;

import java.nio.file.Path;

public class ParkingSubsystemDemo {
    public static void main(String[] args) {
        Path directory = Path.of("demo-data", "space");
        ParkingService service = new ParkingService(
                new ParkingRepository(directory), new ParkingFeeCalculator());
        service.reserveSpace("SPACE-DEMO", "P-01", 2, false);
        System.out.println(service.findReservation("SPACE-DEMO").getStatus());
    }
}
