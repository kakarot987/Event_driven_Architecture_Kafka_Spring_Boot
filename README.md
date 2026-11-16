# Event-driven Order Choreography — Spring Boot + Kafka

**Status: CI-verified local architecture demonstration. NOT production-ready.**

This repository demonstrates event-driven order processing across four business services using Spring Boot, Apache Kafka, MySQL, local transactional outboxes, and idempotent event handlers. It processes **simulated payments**, generates **local shipment records**, and logs **simulated notifications**. It does **not** integrate a payment provider, shipping carrier, or production email/notification delivery.

For the source-backed audit and deployment blockers, read [ARCHITECTURE_SECURITY_AUDIT.md](ARCHITECTURE_SECURITY_AUDIT.md) first. For the operator replay runbook, see [RELIABILITY.md](RELIABILITY.md).

## How it works

```text
POST /api/orders
    |
    v
order-service      MySQL: order + outbox         -> order-events
    |
    v
payment-service    MySQL: mock payment + inbox + outbox -> payment-events
    |
    v
inventory-service  MySQL: reserved stock + inbox + outbox -> inventory-events
    |
    v
shipping-service   MySQL: shipment + inbox + outbox -> shipping-events
    |
    v
order-service receives payment/inventory/shipping outcomes -> order status
```

- The **order service** persists the order and initial event in one DB transaction.
- **Payment, inventory, and shipping** persist business changes, deduplicated inbound event IDs, and outgoing outbox events in one local transaction.
- Scheduled outbox pollers hold row locks, publish to Kafka, **await broker acknowledgement**, and retry with backoff. After eight failed attempts, a row becomes `FAILED` and requires operator review.
- Inventory reservations lock product rows and enforce unique `(orderId, productId)` records.
- Acknowledgement followed by a database crash may still cause re-publication. **Delivery is at least once, not exactly once**, and is not a distributed transaction.
- The gateway has one order-service route. Config Server, Eureka, Vault, monitoring and the full Compose stack are optional modules, **not** part of the verified core environment.

## Evidence: what passed

The tests below ran against real Kafka, MySQL and Spring Boot containers in GitHub Actions, except for the Gradle build.

| Verified check | Evidence | Exact scope |
| --- | --- | --- |
| Multi-module build and Java tests on merged `main` | [Continuous Build](https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43810780820) | JDK 23, Gradle `clean build`; not a security audit |
| Core order → payment → inventory → shipment | [Core E2E](https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43808339911) | Disposable Compose, deterministic mock payment, seeded stock, eventual `SHIPPED` |
| Outbox persistence, duplicate event replay and broker interruption | [Core E2E test](scripts/core_e2e.py) | Four `PUBLISHED` outboxes, one re-delivery and order-outbox broker recovery |
| Gateway forwarding and unknown-route rejection | [Reliability E2E](https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43808339924) | **Eureka and Config Server disabled** |
| Concurrent inventory reservations | [Reliability E2E](scripts/reliability_e2e.py) | Three inventory replicas; 12 competing orders, four stock units: four shipped, eight failed, no oversell |
| DLT publish and manually audited replay | [Reliability E2E](https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43808339924) | Injected failure on `payment-events` leading to real DLT entry; original event replayed once to successful shipment |
| Terminal failed-outbox recovery | [Reliability E2E](scripts/reliability_e2e.py) | Terminal `FAILED` row was **simulated**, manually requeued and audited; actual eight-attempt exhaustion **not** tested |
| Password-reset token properties | [Unit tests](microservices/authentication-authorization/src/test/java/com/eventDrivenKafka/Authentication_Authorization/register/services/PasswordResetServiceTest.java) | Expiry, replay rejection, invalid/unknown tokens; **SMTP and full account lifecycle unverified** |

Evidence is for the linked commits and demonstrated scenarios only. A passing CI pipeline does not establish public-deployment safety, exhaustive crash recovery, or the absence of vulnerabilities.

## Run the isolated core demonstration

**Prerequisites:** Docker Compose v2, Python 3, adequate Docker resources, and a disposable local environment. The core stack contains Kafka, MySQL, and the four business services. It binds the order HTTP endpoint to **127.0.0.1:8081**. The test seeds data in the disposable MySQL volume.

```sh
export DB_PASSWORD="$(openssl rand -hex 20)"
docker compose -f compose.core.yml config --quiet
docker compose -f compose.core.yml up -d --build
python3 scripts/core_e2e.py
docker compose -f compose.core.yml down --volumes --remove-orphans
```

If starting for the first time or troubleshooting topic/consumer readiness, follow the [CI workflow](.github/workflows/CoreE2E.yml), which starts Kafka first, pre-creates its event/DLT topics, and verifies stable consumer groups. Do not run `down --volumes` against databases containing valuable data.

To reproduce the gateway, multi-instance stock, failed-outbox and DLT scenarios, follow [ReliabilityE2E.yml](.github/workflows/ReliabilityE2E.yml) and [RELIABILITY.md](RELIABILITY.md). The `compose.reliability.yml` overlay activates a **test-only fault injector**.

## Build

Requires **JDK 23**:

```sh
chmod +x ./gradlew
./gradlew clean build
```

## Security and production limitations

**Do not expose this repository's example services or Compose stacks to other users.** Significant hardening remains:

1. **Credential history and dev secrets:** rotate any historical JWT, database, SMS, Eureka or Vault values ever used outside a throwaway demo. The legacy Compose/Vault and SQL seed files still contain development-only secrets or root-token assumptions. Untracking a value does not revoke it.
2. **Authorization:** the core demo intentionally permits unauthenticated order requests. Inventory write endpoints have no application-level authorization and shipping read endpoints lack resource-ownership checks. Gateway routing does not substitute for authentication/authorization at each service.
3. **Account recovery:** hashed, short-lived, single-use password-reset tokens and a disabled legacy mobile flow are implemented; however, existing stateless JWTs are **not revoked** when the password changes, rate limiting and verified-email onboarding need work, and real SMTP delivery has not been tested.
4. **Operational reliability:** outbox publishing remains **at least once**; pending/failed rows need alerts, and recovery requires an operator. Additional concurrent-duplicate, out-of-order status, multi-product contention and downstream crash tests are outstanding.
5. **Infrastructure:** the full `docker-compose.yml`, Eureka, Config Server, Vault, Terraform, production TLS, Kafka authentication and monitoring stack are **not validated as a secure deployment**. The core uses plaintext Kafka and disposable MySQL root credentials.
6. **Mocked integrations:** payment outcomes are simulated and notification output is logging-only. No production provider integration or compensation/refund workflow is validated.

**`core-demo`** disables order authentication for local tests; **`reliability-test`** intentionally introduces failure. **Neither profile must be enabled in a network-accessible environment.**

See [SECURITY.md](SECURITY.md) for recovery and credential precautions, [ARCHITECTURE_SECURITY_AUDIT.md](ARCHITECTURE_SECURITY_AUDIT.md) for prioritized findings and source references, and [ROADMAP.md](ROADMAP.md) for remaining work.

### Historical documentation

Older files—`COMPLETION_REPORT.md`, `FINALIZATION_SUMMARY.md`, `EXECUTIVE_SUMMARY.txt`, `QUICK_REFERENCE.md`, and `GIT_WORKFLOW.md`—document earlier implementation intentions and contain outdated completion/production-readiness claims. **Treat them as historical notes, not current audit or deployment evidence.** This README, the audit and linked passing CI runs are the source of truth for current verified claims.

## Portfolio description

> Built a four-stage Spring Boot/Kafka order choreography with locally transactional outboxes, idempotent consumers, MySQL-backed inventory reservations, Docker-backed end-to-end tests, and targeted gateway, contention and failure-replay verification. Documented at-least-once delivery and production/security limitations.
