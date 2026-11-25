# Hotel Booking Service

A Spring Boot REST API for onboarding hotel properties, searching availability, creating bookings, processing payments, and cancelling bookings with refunds.

## Tech stack

- Java 21
- Spring Boot 4.2
- Spring MVC and Jakarta Bean Validation
- Springdoc OpenAPI 3.2.1 (OpenAPI 3 + Swagger UI)
- Gradle
- JUnit 5, Spring Boot Test, and MockMvc

The current implementation uses in-memory repositories and a mock payment gateway so the booking lifecycle can be exercised without external infrastructure.

## API documentation

The API contract is generated from the Spring MVC controllers and request/response DTOs using Springdoc OpenAPI.

Start the application:

```bash
./gradlew bootRun
```

Then open:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml

Swagger UI lets you inspect the endpoints, view generated request/response schemas, and execute API calls against the local application. Bean Validation constraints are reflected in the generated schemas.

## Features

### Property onboarding and search

- Hotel owners can be created and properties onboarded with one or more room types.
- Search supports city, date range, guest count, price, star rating, and amenities through composable search filters.
- Availability is calculated per room type and night.

### Booking and inventory

- Booking requests validate guest capacity and stay dates.
- Inventory is reserved atomically while holding a lock scoped to the room type.
- A booking starts in `PENDING_PAYMENT`.
- A successful payment moves it to `CONFIRMED`; a failed payment leaves it pending so the guest can retry with a new idempotency key.

### Payments

- Payment methods are selected through the `PaymentProcessor` abstraction (`CARD`, `UPI`, and `WALLET`).
- Processors delegate to a `PaymentGateway`; the included mock gateway is deterministic and does not contact a real payment provider.
- Every payment request requires an `Idempotency-Key` header. Replaying the same key and request returns the original payment; reusing the key for a different booking or method is rejected.

### Cancellation and refunds

- Cancel with `POST /api/bookings/{bookingId}/cancel`.
- Cancellation at least two calendar days before check-in receives a 100% refund; cancellation inside that two-day window receives a 50% refund.
- Unpaid bookings can be cancelled without a refund.
- A successful refund is recorded before inventory is released and the booking is marked `CANCELLED`.
- Cancellation is serialized with payment for the booking and uses the same room-type lock as booking creation.
- Repeated cancellation returns the existing cancellation result and does not refund or release inventory again.

## Run locally

Requirements: JDK 21.

Start the application:

```bash
./gradlew bootRun
```

Run all tests:

```bash
./gradlew test
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

The service starts on port 8080 by default.

## API walkthrough

The following examples show the main happy path. Replace IDs with values returned by the preceding request.

### 1. Create an owner

```bash
curl -X POST http://localhost:8080/api/owners \
  -H 'Content-Type: application/json' \
  -d '{ "name": "Grand Hotels Group" }'
```

### 2. Onboard a property

```bash
curl -X POST http://localhost:8080/api/owners/{ownerId}/properties \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Lakeview Hotel",
    "addressLine": "12 MG Road",
    "locality": "Indiranagar",
    "city": "Bengaluru",
    "starRating": 4,
    "amenities": ["WIFI", "POOL"],
    "roomTypes": [
      {
        "name": "Deluxe",
        "maxOccupancy": 2,
        "totalRooms": 5,
        "pricePerNight": 3500
      }
    ]
  }'
```

Use the returned property ID and room type ID in subsequent requests.

### 3. Search properties

```bash
curl -G http://localhost:8080/api/properties/search \
  --data-urlencode 'city=Bengaluru' \
  --data-urlencode 'checkIn=2027-01-10' \
  --data-urlencode 'checkOut=2027-01-12' \
  --data-urlencode 'guests=2'
```

### 4. Create a booking

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H 'Content-Type: application/json' \
  -d '{
    "propertyId": "{propertyId}",
    "roomTypeId": "{roomTypeId}",
    "checkIn": "2027-01-10",
    "checkOut": "2027-01-12",
    "guests": 2
  }'
```

The response contains the booking ID and starts with status `PENDING_PAYMENT`.

### 5. Pay for the booking

```bash
curl -X POST http://localhost:8080/api/bookings/{bookingId}/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: booking-payment-001' \
  -d '{ "method": "UPI" }'
```

The mock gateway succeeds by default. The booking then becomes `CONFIRMED`. Supported methods are `CARD`, `UPI`, and `WALLET`.

### 6. Retrieve a booking

```bash
curl http://localhost:8080/api/bookings/{bookingId}
```

### 7. Cancel and refund

```bash
curl -X POST http://localhost:8080/api/bookings/{bookingId}/cancel
```

The response includes `bookingId`, `status`, `refundAmount`, and `paymentId`. Calling the cancellation endpoint again is safe and does not issue a second refund.

## Main API endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/owners` | Create an owner |
| POST | `/api/owners/{ownerId}/properties` | Onboard a property and room types |
| GET | `/api/properties/{propertyId}` | Get property details |
| GET | `/api/properties/search` | Search properties and availability |
| POST | `/api/bookings` | Create a booking and reserve inventory |
| GET | `/api/bookings/{bookingId}` | Get booking details |
| POST | `/api/bookings/{bookingId}/payments` | Process a payment (requires `Idempotency-Key`) |
| POST | `/api/bookings/{bookingId}/cancel` | Cancel a booking and refund when applicable |

## Design notes and current limitations

- **Concurrency:** in-process locks protect room-type inventory updates and serialize payment/cancellation operations for a booking. They do not coordinate multiple application instances.
- **Persistence:** repositories are in-memory; data is lost when the process stops. A production deployment should use a transactional database and durable idempotency records.
- **Payments:** the mock gateway is for local development and tests only. A production gateway integration must handle provider timeouts and ambiguous outcomes using provider-side idempotency and reconciliation.
- **Refund policy:** the two-day threshold uses calendar dates in the server's local date context. Refund calculation is isolated behind `CancellationPolicy` so the policy can be replaced.
- **Atomicity:** the in-memory flow demonstrates ordering and locking but is not a substitute for database transactions, an outbox, or a durable saga when integrating external payment systems.

## Project structure

```text
src/main/java/com/hotelbooking/hotel_booking/
  config/           API documentation configuration
  controller/       REST controllers
  dto/              Request and response models
  enums/            Booking, payment, and property enums
  exception/        Domain exceptions and API error mapping
  lock/             Lock abstraction and in-memory implementation
  model/            Domain entities and value objects
  repository/       Repository interfaces and in-memory adapters
  service/           Application services
    cancellation/   Cancellation policy
    filter/         Composable property search filters
    impl/           Service implementations
    payment/        Payment processors and gateway abstraction
  util/              Validation helpers

src/test/java/       Unit and Spring MVC integration tests
```
