package kz.edu.parking;

import kz.edu.parking.common.GarageException;
import kz.edu.parking.entry.EntryRepository;
import kz.edu.parking.entry.EntryService;
import kz.edu.parking.entry.LicensePlateValidator;
import kz.edu.parking.facade.FailurePoint;
import kz.edu.parking.facade.OperationHistoryRepository;
import kz.edu.parking.facade.OperationRepository;
import kz.edu.parking.facade.ParkingAdminFacade;
import kz.edu.parking.facade.ParkingFacade;
import kz.edu.parking.facade.ParkingOperation;
import kz.edu.parking.payment.PaymentAmountValidator;
import kz.edu.parking.payment.PaymentRepository;
import kz.edu.parking.payment.PaymentService;
import kz.edu.parking.space.ParkingFeeCalculator;
import kz.edu.parking.space.ParkingRepository;
import kz.edu.parking.space.ParkingService;

import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path dataDirectory = Path.of("data");

        EntryService entryService = new EntryService(
                new EntryRepository(dataDirectory), new LicensePlateValidator());
        ParkingService parkingService = new ParkingService(
                new ParkingRepository(dataDirectory), new ParkingFeeCalculator());
        PaymentService paymentService = new PaymentService(
                new PaymentRepository(dataDirectory), new PaymentAmountValidator());
        OperationRepository operationRepository = new OperationRepository(dataDirectory);
        OperationHistoryRepository historyRepository = new OperationHistoryRepository(dataDirectory);

        ParkingFacade parkingFacade = new ParkingFacade(
                entryService, parkingService, paymentService,
                operationRepository, historyRepository);
        ParkingAdminFacade adminFacade = new ParkingAdminFacade(
                entryService, parkingService, paymentService,
                operationRepository, historyRepository);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine();

            try {
                if (choice.equals("1")) {
                    startOperation(scanner, parkingFacade, FailurePoint.NONE);
                } else if (choice.equals("2")) {
                    startOperation(scanner, parkingFacade, FailurePoint.SUBSYSTEM_B);
                } else if (choice.equals("3")) {
                    startOperation(scanner, parkingFacade, FailurePoint.SUBSYSTEM_C);
                } else if (choice.equals("4")) {
                    showOperations(adminFacade);
                } else if (choice.equals("5")) {
                    System.out.print("Operation id: ");
                    String id = scanner.nextLine();
                    ParkingOperation operation = adminFacade.compensateManually(id);
                    System.out.println("New status: " + operation.getStatus());
                } else if (choice.equals("6")) {
                    System.out.print("Operation id: ");
                    String id = scanner.nextLine();
                    ParkingOperation operation = parkingFacade.resume(id, FailurePoint.NONE);
                    System.out.println("New status: " + operation.getStatus());
                } else if (choice.equals("7")) {
                    running = false;
                } else {
                    System.out.println("Unknown menu option");
                }
            } catch (GarageException | NumberFormatException exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            }
        }
    }

    private static void startOperation(Scanner scanner, ParkingFacade facade, FailurePoint failurePoint) {
        System.out.print("Operation id: ");
        String id = scanner.nextLine();
        System.out.print("License plate: ");
        String licensePlate = scanner.nextLine();
        System.out.print("Parking space (for example P-01): ");
        String space = scanner.nextLine();
        System.out.print("Parking hours: ");
        int hours = Integer.parseInt(scanner.nextLine());
        System.out.print("Payment amount in KZT (500 KZT per hour): ");
        int amount = Integer.parseInt(scanner.nextLine());

        ParkingOperation operation = facade.startParking(
                id, licensePlate, space, hours, amount, failurePoint);
        System.out.println("Operation status: " + operation.getStatus());
    }

    private static void showOperations(ParkingAdminFacade adminFacade) {
        if (adminFacade.findAllOperations().isEmpty()) {
            System.out.println("No saved operations");
            return;
        }

        for (ParkingOperation operation : adminFacade.findAllOperations()) {
            System.out.println(adminFacade.inspect(operation.getId()));
            System.out.println("History: " + adminFacade.findHistory(operation.getId()));
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== Smart Parking Garage ===");
        System.out.println("1. Complete parking operation");
        System.out.println("2. Demonstrate failure in subsystem B");
        System.out.println("3. Demonstrate failure in subsystem C");
        System.out.println("4. Inspect persisted state");
        System.out.println("5. Run manual compensation");
        System.out.println("6. Resume persisted operation");
        System.out.println("7. Exit");
        System.out.print("Choose an option: ");
    }
}
