# Event-driven Kafka order choreography (Spring Boot)

A system-design and reliability demonstration: **order → mock payment → inventory reservation → shipment** across Spring Boot services, Kafka topics and MySQL. Notification delivery remains simulated. **Not production-ready**, and no real payment processing is performed.

## Verified core architecture

```text
POST /api/orders
  order-service -> orders DB + order outbox -> order-events
  payment-service -> payment DB + payment outbox -> payment-events
  inventory-service -> locked stock reservation + inventory outbox -> inventory-events
  shipping-service -> shipments DB + shipping outbox -> shipping-events
  order-service <- payment/inventory/shipping events update order status (SHIPPED)
```

Each downstream consumer uses a local database transaction to commit its **business result + processed-event record + outgoing outbox event** together. Each service polls its outbox with database row locks, waits for Kafka broker acknowledgment, retries with exponential backoff and exposes exhausted deliveries as `FAILED` rows requiring operational attention.

**This is at-least-once delivery, not exactly-once Kafka/database transactions.** A crash after broker ack and before database commit can republish the event. Event identifiers and business uniqueness constraints are used for deduplication. Pessimistic polling currently holds row locks during broker acknowledgment.

## Run the verified minimal stack

Requires Docker Compose v2, Python 3 and a disposable local environment. The core profile starts **six containers**: MySQL, Kafka and four business services. It does not require Vault, Config Server, Eureka, the gateway or the monitoring stack.

```sh
export DB_PASSWORD="$(openssl rand -hex 20)"
docker compose -f compose.core.yml config --quiet
docker compose -f compose.core.yml up -d --build
python3 scripts/core_e2e.py
docker compose -f compose.core.yml down --volumes
```

The test itself creates/seed products in the **disposable core MySQL volume** and never uses live customer data. The local order API is reachable at `http://127.0.0.1:8081/api/orders` only. The `core-demo` Spring profile permits unauthenticated orders **solely for this loopback-bound demo**; do not enable it on a network-accessible deployment. Payment success is forced in the core demo for deterministic testing; normal payment outcomes are randomly simulated.

`scripts/core_e2e.py` verifies:
- API order creation and eventual `SHIPPED` order state
- successful payment, one inventory reservation, one shipment, and correct remaining stock in MySQL
- `PUBLISHED` outbox records in **all four** databases
- re-delivery of the original Kafka event without duplicate payment, reservation, shipment, or stock decrement
- order created while Kafka is stopped remaining in the order outbox, then reaching `SHIPPED` after broker restart

GitHub Actions **Core Kafka / MySQL choreography E2E** passed on November 16, 2026: [view run](https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43802446235). The multi-module Gradle build was also green for the implementation.

## Build all modules

Requires JDK 23:

```sh
chmod +x ./gradlew
./gradlew clean build
```

The `Continuous Build` workflow exercises this command on pull requests. The original order-service embedded Kafka tests check event structure and serialization; the separate Compose workflow is the **real four-service integration test**.

## Configuration & security

Copy `.env.example` to an untracked `.env` for non-core development. The full `docker-compose.yml` is a larger legacy stack with optional monitoring/discovery infrastructure and **has not** received the same live E2E validation. Database schema scripts, ports and service credentials must match your environment.

The repository historically contained committed credentials, a signing key and an SMS provider key. Removing these files does **not** revoke secrets embedded in Git history. See [SECURITY.md](SECURITY.md) for mandatory rotation and password-reset deployment precautions.

## Remaining limitations

- Tests verify the core **happy path, one replay, and an order-outbox broker outage**. They do not exhaustively simulate every crash point in each downstream service, DLT replay, network partition, or multi-instance contention.
- Business key uniqueness and inbox/outbox behavior under **concurrent duplicates** still need stress testing. Outbox `FAILED` records require monitoring and manual recovery.
- Payment remains mock/random outside the deterministic demo profile; no real payment provider or refund/compensation orchestration exists.
- Inventory needs products seeded. No automated supplier stock-management flow is included.
- Notifications only log simulated messages. DLT routing targets partition 0, without an operational replay console.
- Do not use the `core-demo` security profile or test credentials in production. Validate any publicly accessible gateway, authentication, secret rotation, TLS and SMTP integration separately.
- Terraform describes a local sample, not a production deployment.

See [ROADMAP.md](ROADMAP.md) for remaining verification and operational work.
