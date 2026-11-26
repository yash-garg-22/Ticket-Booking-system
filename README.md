Ticket Booking System
A backend ticket-booking application built with Java 21 and Spring
Boot. The project demonstrates how to handle concurrent booking
requests, coordinate transaction processing, and maintain booking
consistency using Redis distributed locks, MySQL, and Apache
Kafka.
Key Features
- Prevents double booking: Uses Redisson distributed locks to
  coordinate concurrent booking attempts for the same seat.
- Transaction lifecycle management: Tracks transaction states such
  as PENDING, SUCCESS, FAILED, and EXPIRED.
- Asynchronous event processing: Publishes transaction events to
  Kafka for downstream processing.
- Atomic status transitions: Uses conditional database updates so
  that only eligible PENDING transactions can move to a final state.
- Pending transaction expiry: A scheduled task identifies
  transactions that remain pending beyond the configured time window
  and marks them as expired.
- Database transaction management: Uses Spring's @Transactional
  support for operations that need to succeed or roll back together.
Tech Stack
  Technology        Purpose
  Java 21           Application development
Spring Boot       Backend application framework
Spring Data JPA   Database access
MySQL             Persistent booking and transaction data
Redis             Distributed lock coordination
Redisson          Java client for Redis-based locking
Apache Kafka      Transaction event messaging
Docker            Running infrastructure services locally
Maven             Dependency and build management
Booking Flow
1. A user submits a request to book a seat.
2. The application attempts to acquire a Redis distributed lock for
   that seat.
3. If the lock is acquired, the booking/payment flow proceeds;
   competing requests for the same seat cannot proceed concurrently
   while the lock is held.
4. The transaction is recorded and its event is published to Kafka.
5. The application processes the transaction result and conditionally
   updates the database.
6. On successful confirmation, the seat is marked as booked.
7. A scheduled task checks old PENDING transactions and marks those
   past the expiry window as EXPIRED.
Transaction States
  State       Meaning
  PENDING   Transaction is awaiting a final outcome
SUCCESS   Transaction completed and booking was confirmed
FAILED    Transaction failed
EXPIRED   A pending transaction exceeded the configured time limit
Infrastructure
The project uses the following services during local development:
- MySQL --- host port 33306 mapped to container port 3306
- Redis --- port 6379
- Kafka --- port 9092
- ZooKeeper --- port 2181 (used by the configured Kafka setup)
These services can be run locally or through the Docker setup included
in the project.
Getting Started
Prerequisites
- JDK 21
- Maven
- Docker Desktop (if running the infrastructure in containers)
- MySQL, Redis, and Kafka
Run the application
1. Clone this repository:
   git clone <YOUR_REPOSITORY_URL>
   cd TicketBooking
2. Start the required infrastructure services (MySQL, Redis, Kafka, and
   ZooKeeper) using your Docker configuration.
3. Update the application configuration with the correct database,
   Redis, and Kafka connection details for your environment.
4. Build the project:
   mvn clean package
5. Run the Spring Boot application:
   mvn spring-boot:run
Configuration note: Do not commit real database passwords, API
keys, or other secrets. Keep environment-specific values outside the
repository.

Engineering Concepts Demonstrated
- Distributed locking for concurrent request coordination
- Race-condition and duplicate-booking prevention
- Event-driven communication with Kafka
- Conditional/atomic database state transitions
- Scheduled background processing
- Transaction management with Spring
Future Improvements
- Add authentication and role-based authorization.
- Integrate a real payment gateway.
- Add retry and dead-letter handling for failed Kafka messages.
- Add automated unit and integration tests.
- Add API documentation with OpenAPI/Swagger.
Note: This is a learning project demonstrating backend concepts for
concurrent ticket booking and transaction processing.
