# Smart Parking Garage

Assignment 4 demonstrates the Facade pattern over three independent subsystems with durable file storage and compensation after partial failure.

## Domain and chain

The client starts one parking operation through `ParkingFacade`:

1. Subsystem A admits a vehicle through the gate.
2. Subsystem B reserves a parking space and calculates the fee.
3. Subsystem C validates and completes the payment.

Each subsystem has its own entity, status enum, business rule, service, and CSV persistence gateway. The subsystems do not import or call one another. They meet only inside the facades.

## Facades

- `ParkingFacade` runs the common A -> B -> C workflow, saves progress, resumes interrupted work, and compensates partial work after failure.
- `ParkingAdminFacade` inspects persisted operations and performs manual compensation.

The operation statuses are `STARTED`, `PENDING_B`, `PENDING_C`, `COMPLETED`, `COMPENSATED`, and `FAILED`. They are stored in `data/operations.csv`, so progress survives an application restart.

## Compensation rules

- If B fails after A committed, the facade cancels A.
- If C fails after A and B committed, the facade cancels B first and A second.
- For manual compensation of a completed operation, the admin facade refunds C, cancels B, and then cancels A.

The compensation order is explicit in `ParkingFacade`: subsystem B is cancelled before subsystem A. The tests verify the persisted final state of both subsystems.

## Build and run

Requirements: Java 17 or newer.

Run all tests, generate the JaCoCo report, and enforce at least 80% line coverage:

```powershell
.\mvnw.cmd clean verify
```

The coverage report is generated at `target/site/jacoco/index.html`.

Run the console demo:

```powershell
.\mvnw.cmd exec:java
```

The menu can run a successful chain, force a B failure, force a C failure, inspect saved state, run manual compensation, and resume a saved operation.

Reset the durable store before a fresh demonstration:

```powershell
Remove-Item -Recurse -Force data
```

The application recreates the storage files during its next run.

## Independent subsystem demonstrations

Each subsystem can be used without either facade:

```powershell
.\mvnw.cmd exec:java -Dexec.mainClass=kz.edu.parking.demo.EntrySubsystemDemo
.\mvnw.cmd exec:java -Dexec.mainClass=kz.edu.parking.demo.ParkingSubsystemDemo
.\mvnw.cmd exec:java -Dexec.mainClass=kz.edu.parking.demo.PaymentSubsystemDemo
```

## Principle of Least Knowledge

Without the facade, a client would need to know every subsystem, its repositories, the correct order, and the compensation rules:

```java
entryService.admitVehicle(id, plate);
ParkingReservation reservation = parkingService.reserveSpace(id, space, hours, false);
paymentService.processPayment(id, reservation.getFee(), amount, false);
```

It would also need to catch failures and call `cancelReservation` and `cancelEntry` in the correct order. This gives the client many direct friends and exposes internal workflow details.

With the facade, the client has one immediate friend and calls one coherent operation:

```java
parkingFacade.startParking(id, plate, space, hours, amount, FailurePoint.NONE);
```

## Why Facade instead of Adapter

The problem needs a Facade because the existing subsystem interfaces are already usable. The goal is to provide one simple operation that coordinates their order, persistence, recovery, and compensation. An Adapter would solve a different problem: it would translate an incompatible interface into an interface expected by a client. No interface conversion is required here. `ParkingFacade` keeps the subsystem interfaces unchanged and hides the complexity of using them together.

## Tests

JUnit 5 tests use a new temporary directory for every test, so they are fast, independent, repeatable, and self-validating. `GarageTestDsl` provides domain-language helpers such as `whenSubsystemBFails` and `thenOperationIs`. Tests cover the happy path, both compensation depths and their persisted final states, persistence reload after a simulated restart, invalid input, occupied spaces, duplicate operations, and administrative compensation.

## UML

The final class diagram is available at `docs/uml.svg`. Its editable source is `docs/uml.puml`.
