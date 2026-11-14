# Event-driven order choreography with Spring Boot and Kafka

A **learning and system-design project** demonstrating order, mock payment, inventory reservation and shipping services communicating through Kafka topics. This repository is not production-ready and does not process real payments or send real notifications.

## Architecture

```text
HTTP POST /api/orders
    |
order-service -> orders DB + transactional outbox
    |
  order-events -> payment-service (mock/random success)
                       |
                 payment-events -> inventory-service
                                      |
                                inventory-events -> shipping-service
                                                        |
                                                  shipping-events
```

The notification service consumes order, payment and inventory event types and currently logs *simulated* messages. Spring Cloud Config, Eureka and gateway are optional infrastructure; no verified production deployment is provided.

## Building

Requires **JDK 23** and Docker if running the infrastructure. From the repository root:

```sh
chmod +x ./gradlew
./gradlew clean build
```

The order-service tests currently exercise event JSON structure and an embedded Kafka send; they are **not** a four-service end-to-end verification. A new authentication test suite covers password-reset token edge cases but has not yet been executed in the connected environment.

## Configuration

Copy `.env.example` to `.env`, set unique secrets, and **never commit it**. The Compose stack requires variables including `DB_PASSWORD`, `JWT_SECRET` and any service-specific configuration. Use a proper secrets manager for any environment shared with others. Actual database schemas/users must match the MySQL initialization scripts; review them before attempting startup.

The original `.env`, JWT signing secret, provider key and test credentials existed in Git history. Removing current files does **not** rotate them: see [SECURITY.md](SECURITY.md). Password recovery requires configured SMTP and an HTTPS reset page; mobile OTP endpoints are intentionally disabled.

## Local developer workflow

1. Configure an isolated database and a Kafka broker (see `docker-compose.yml` for infrastructure reference). Compose includes extra monitoring and service-discovery components, so it is **not yet a validated minimal quickstart**.
2. Supply `DB_PASSWORD`, `JWT_SECRET`, `KAFKA_BOOTSTRAP_SERVERS` and database JDBC URLs to the services.
3. Build modules with the root Gradle wrapper. Start infrastructure, then services, with credentials injected only at runtime.
4. Call `POST /api/orders` with a JSON body matching `CreateOrderRequest`; query `GET /api/orders/{orderId}` for the persisted order. Use service actuator/health endpoints and Kafka consumers to observe events.

## Known limitations / outstanding work

- Payment is randomly simulated and inventory must be seeded with products for successful reservations.
- The **order outbox** waits for Kafka acknowledgement, retries with backoff, and marks an exhausted event `FAILED`; monitor and investigate failures. Delivery remains **at least once**, and duplicates are possible after ack-before-commit crashes.
- Payment, inventory and shipping event publication now waits for acknowledgement before marking incoming events processed. This reduces loss windows but does **not** provide an atomic database/outgoing-event transaction. Extend the transactional outbox to each service and test crash points before claiming lossless choreography.
- Inventory now uses database row locks for stock checks; this has not yet been stress-tested for contention or duplicate concurrent order events.
- Order status listeners consume downstream payment/inventory/shipping outcomes, but ordering/race behavior still needs end-to-end verification.
- Dead-letter recoverers send all failures to partition 0 on each one-partition DLT. DLT replay and alerting remain manual.
- CI and service-to-service integration have not been independently verified. The Terraform setup is a local demo, not a stack deployment.
- Email recovery needs rate limiting and security review. Previously issued JWTs remain valid until expiry unless signing keys are rotated or token revocation is implemented.

See [ROADMAP.md](ROADMAP.md) for the prioritized checklist.
