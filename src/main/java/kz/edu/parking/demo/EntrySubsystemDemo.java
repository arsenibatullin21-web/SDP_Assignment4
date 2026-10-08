package kz.edu.parking.demo;

import kz.edu.parking.entry.EntryRepository;
import kz.edu.parking.entry.EntryService;
import kz.edu.parking.entry.LicensePlateValidator;

import java.nio.file.Path;

public class EntrySubsystemDemo {
    public static void main(String[] args) {
        Path directory = Path.of("demo-data", "entry");
        EntryService service = new EntryService(
                new EntryRepository(directory), new LicensePlateValidator());
        service.admitVehicle("ENTRY-DEMO", "123ABC01");
        System.out.println(service.findEntry("ENTRY-DEMO").getStatus());
    }
}
