package kz.edu.parking;

import kz.edu.parking.entry.EntryRepository;
import kz.edu.parking.entry.EntryService;
import kz.edu.parking.entry.LicensePlateValidator;
import kz.edu.parking.facade.OperationRepository;
import kz.edu.parking.facade.ParkingAdminFacade;
import kz.edu.parking.facade.ParkingFacade;
import kz.edu.parking.payment.PaymentAmountValidator;
import kz.edu.parking.payment.PaymentRepository;
import kz.edu.parking.payment.PaymentService;
import kz.edu.parking.space.ParkingFeeCalculator;
import kz.edu.parking.space.ParkingRepository;
import kz.edu.parking.space.ParkingService;

import java.nio.file.Path;

class TestGarage {
    final EntryRepository entryRepository;
    final EntryService entryService;
    final ParkingRepository parkingRepository;
    final ParkingService parkingService;
    final PaymentRepository paymentRepository;
    final PaymentService paymentService;
    final OperationRepository operationRepository;
    final ParkingFacade parkingFacade;
    final ParkingAdminFacade adminFacade;

    TestGarage(Path directory) {
        entryRepository = new EntryRepository(directory);
        entryService = new EntryService(entryRepository, new LicensePlateValidator());
        parkingRepository = new ParkingRepository(directory);
        parkingService = new ParkingService(parkingRepository, new ParkingFeeCalculator());
        paymentRepository = new PaymentRepository(directory);
        paymentService = new PaymentService(paymentRepository, new PaymentAmountValidator());
        operationRepository = new OperationRepository(directory);
        parkingFacade = new ParkingFacade(entryService, parkingService, paymentService,
                operationRepository);
        adminFacade = new ParkingAdminFacade(entryService, parkingService, paymentService,
                operationRepository);
    }
}
