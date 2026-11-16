# Architecture and Security Audit

**Reviewed:** 16 November 2026  
**Source baseline:** merged `main` at [826563e](https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/commit/826563e660cc1381ffe0c5d57ae58d7b614bf00c) (PRs #1–#3).  
**Assessment:** **Validated local architecture demonstration; NOT approved for public/production deployment.**

This is a **source-level review** of the core services, authentication, gateway, infrastructure samples, operator scripts, existing tests and GitHub Actions evidence. It is **not** an independent penetration test, deployed-environment audit, comprehensive dependency/SAST/secret-history scan or attestation that credentials were rotated. Findings describe observed implementation and risks conditional on deployment, rather than claiming observed exploitation.

## 1. Evidence and architecture

| Claim | Evidence | Scope / caveat |
| --- | --- | --- |
| Root multi-module build and tests pass on merged `main` | [Continuous Build, main at 826563e](https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43810780820) | A passing build is **not** security verification |
| Order → mock payment → stock reservation → shipment | [Core Kafka/MySQL E2E](https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43808339911) | Disposable Compose, deterministic mock payment, seeded stock |
| Four `PUBLISHED` outbox records, original-order-event replay, broker outage/recovery | [Core E2E workflow and script](.github/workflows/CoreE2E.yml) / [script](scripts/core_e2e.py) | Specific tested failure windows, not exhaustive crash testing |
| Real Spring Cloud Gateway forwarding | [Reliability E2E](https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43808339924) | Eureka and Config Server **disabled** |
| 12 concurrent orders, 4 available stock units, 3 inventory replicas: exactly 4 shipped | [Reliability E2E](https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43808339924) | One controlled contention scenario, not generalized stress proof |
| DLT publish → single audited replay → shipment; manual outbox requeue | [Reliability E2E](https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43808339924) | DLT failure was injected under `reliability-test`; terminal `FAILED` row was **simulated**, not exhausted naturally |
| Password reset token conditions | [PasswordResetService](microservices/authentication-authorization/src/main/java/com/eventDrivenKafka/Authentication_Authorization/register/services/PasswordResetService.java) and [unit tests](microservices/authentication-authorization/src/test/java/com/eventDrivenKafka/Authentication_Authorization/register/services/PasswordResetServiceTest.java) | Unit tested; **no live SMTP or authentication-service E2E proof** |

### Data flow and boundaries

```text
Local HTTP request -> order-service -> order_db [order + outbox]
                                          |
                                      order-events
                                          v
                              payment-service -> payment_db [payment + inbox + outbox]
                                          |
                                     payment-events
                                          v
                              inventory-service -> inventory_db [locked stock + inbox + outbox]
                                          |
                                    inventory-events
                                          v
                              shipping-service -> shipping_db [shipment + inbox + outbox]
                                          |
                                    shipping-events
                                          v
                      order-service consumers update order status

Each service polls its own outbox, awaits Kafka acknowledgement, and retries.
Order/payment/inventory/shipping events may be redelivered; there is no
cross-service exactly-once transaction, external payment or compensation saga.
```

In the tested demo, Kafka and MySQL run in containers, the HTTP order endpoint binds to loopback, and the optional Gateway uses `gateway-test`. `core-demo` deliberately permits unauthenticated order operations; `reliability-test` deliberately injects failures. Neither profile is suitable for a shared/public system.

## 2. Security findings

Severity reflects the consequence **if the current example stack were deployed outside its isolated local test environment**. The absence of evidence for a deployed exploit does not make a known control unnecessary.

| ID | Priority | Finding and evidence | Required remediation |
| --- | --- | --- | --- |
| SEC-01 | **P0 — deployment blocker** | Historically committed credentials are not revoked by untracking files. In the current tree, [legacy Compose](docker-compose.yml) uses a Vault **development root token** and [Vault seed](vault/seed.sh) and [MySQL initialization](mysql-init/01-create-users.sql) contain sample credential material. Their previous real-world use and rotation cannot be verified from Git. | Inventory and rotate any values used beyond an isolated throwaway demo (JWT signing keys, DB/Eureka/Vault/SMS keys), invalidate signed sessions as applicable, remove tracked literal credentials from example scripts, use least-privilege secret storage, review CI/logs/forks/history. Do **not** expose or reuse the sample root token. |
| SEC-02 | **P0 — deployment blocker** | [InventoryController](microservices/inventory-service/src/main/java/com/eventDrivenKafka/inventoryservice/controller/InventoryController.java) exposes product writes and reads with no application-level authorization; inventory module lacks a Spring Security starter. [ShippingController](microservices/shipping-service/src/main/java/com/eventDrivenKafka/shippingservice/controller/ShippingController.java) lists shipments without an application-level authorization layer; [gateway routes](spring_cloud/gateway/src/main/resources/application.yml) are not an authenticated authorization boundary. The `core-demo` order API is deliberately open. | Keep business services private; implement and test authentication and per-resource authorization at the gateway **and** services, restrict product mutation to administrators, check order/shipments ownership, add network policy/TLS and negative authorization tests. Never enable demo profiles publicly. |
| SEC-03 | **P1 — high** | [PasswordResetService](microservices/authentication-authorization/src/main/java/com/eventDrivenKafka/Authentication_Authorization/register/services/PasswordResetService.java) clears the stored `authToken`, but [JwtUtils](microservices/authentication-authorization/src/main/java/com/eventDrivenKafka/Authentication_Authorization/register/util/JwtUtils.java) validates signature/expiry without consulting that field. Previously issued JWTs can remain usable until expiry. | Add token version/revocation or rotate signing keys as part of incident response; test rejection of pre-reset tokens. |
| SEC-04 | **P1 — high** | Reset requests have no application-level throttling. [PasswordResetService](microservices/authentication-authorization/src/main/java/com/eventDrivenKafka/Authentication_Authorization/register/services/PasswordResetService.java) can throw a configuration error **only after** finding a verified account when SMTP/reset URL is missing, creating an account-discovery difference in misconfigured environments. [Signup](microservices/authentication-authorization/src/main/java/com/eventDrivenKafka/Authentication_Authorization/controller/RegisterUserServiceImpl.java) creates `emailVerified=false` and immediately issues a JWT; no end-to-end verified-email enrollment proof was found. | Implement IP/account rate limiting, uniform response/failure handling, safe mail queuing, end-to-end verified-email and reset tests, and an explicit policy for unverified sign-ins. |
| SEC-05 | **P1 — high** | [WebSecurityConfig](microservices/authentication-authorization/src/main/java/com/eventDrivenKafka/Authentication_Authorization/register/config/WebSecurityConfig.java) permits all of `/api/register-login/**`; [RegisterUserController](microservices/authentication-authorization/src/main/java/com/eventDrivenKafka/Authentication_Authorization/controller/RegisterUserController.java) uses wildcard cross-origin annotation. Profile operations still compare custom `accessToken` request headers with a DB token instead of using standard principal-based ownership checks. | Explicitly allow only signup/signin/recovery endpoints, restrict CORS origins, use authenticated principals and method-level ownership checks for profile operations, and add forbidden-access tests. |
| SEC-06 | **P1** | [CreateOrderRequest](microservices/order-service/src/main/java/com/eventDrivenKafka/orderservice/dto/CreateOrderRequest.java) accepts client-supplied order totals/item prices; nested items lack explicit validation constraints. Payment is mocked today, but these values cannot be trusted for a real charge. | Validate positive item quantities and product IDs, compute prices/totals server-side from authoritative catalog data, bind users to the authenticated principal. |
| SEC-07 | **P1** | [Config Server](spring_cloud/config-server/src/main/resources/application.yml) exposes all actuator endpoints and has unverified Vault/native profile setup. The full discovery/Vault stack has not been exercised through the verified core workflow. | Narrow actuator exposure, require authentication/TLS, validate Config Server/Eureka/Vault integration in a separately isolated, reproducible test. |

**Previously fixed:** The original unauthenticated email-only password-change operation was replaced with a hashed, 15-minute, single-use emailed token, and unsafe legacy mobile recovery was disabled. These are source-level/unit-tested mitigations, **not** verification of live mail delivery or full account lifecycle safety.

## 3. Architecture and reliability findings

| ID | Priority | Finding and evidence | Next step |
| --- | --- | --- | --- |
| REL-01 | **P1** | [OrderService.updateOrderStatus](microservices/order-service/src/main/java/com/eventDrivenKafka/orderservice/service/OrderService.java) checks a status ranking without a `@Version` field or row lock. A delayed `FAILED` event can overwrite `SHIPPED`, and concurrent listener writes may race. | Define an explicit allowed-transition state machine, use versioning/locking, record event IDs, and test out-of-order/conflicting statuses. |
| REL-02 | **P1** | Payment/inventory/shipping use a **local transaction** for business state, processed-event row, and outgoing outbox row. The [outbox pollers](microservices/payment-service/src/main/java/com/eventDrivenKafka/paymentservice/event/OutboxPublisher.java) hold DB locks while waiting for Kafka acknowledgement, with an eight-attempt terminal `FAILED` state. Ack-then-DB-commit failure can redeliver. | Preserve idempotent consumers, test all crash points and multi-instance pollers, add metrics and alerts for pending/failed outboxes, consider bounded claim/lease design. |
| REL-03 | **P1** | [replay_failed_outbox.py](scripts/replay_failed_outbox.py) and [replay_dlt.py](scripts/replay_dlt.py) rely on local Docker/MySQL access and operator **labels**, not authenticated identities. DLT `CLAIMED` status may require manual reconciliation after an uncertain send. | Keep tools local/CI-only; productionize with authenticated operator approvals, restricted DB privileges, durable audit storage, alerts and reconciliation procedures. |
| REL-04 | **P1** | Duplicate and stock-concurrency tests cover specific replay/contention scenarios, not simultaneous duplicate deliveries for the **same order**, repeated multi-product contention, or an exhaustive downstream crash matrix. | Extend integration tests using deterministic fault injection and repeated high-contention runs. |
| REL-05 | **P2** | [ShippingService](microservices/shipping-service/src/main/java/com/eventDrivenKafka/shippingservice/service/ShippingService.java) emits `SHIPPING_CREATED`/`SHIPPING_FAILED` event-type text, but [replay_dlt.py](scripts/replay_dlt.py) expects `ShippingCreatedEvent` for `shipping-events`. The demonstrated DLT replay covered **payment-events only**. | Normalize event-type names and test each DLT source before advertising generalized DLT replay. |
| OPS-01 | **P1** | `spring.jpa.hibernate.ddl-auto=update` is used; the demo uses a MySQL root user and plaintext Kafka transport. Terraform and the full Vault/monitoring Compose stack have not been demonstrated as production deployment. | Use schema migrations, least-privilege DB users, Kafka authentication/TLS, secure service networking and deployment rehearsals. |
| OPS-02 | **P2** | Payment randomly simulates success outside the demo; shipping generates local tracking IDs; notifications only log a message. No payment provider, shipment carrier, mail provider or compensation/refund process is proven. | Keep these explicitly labeled as mocks until real adapters and rollback/compensation paths are tested. |

## 4. Documentation credibility

[COMPLETION_REPORT.md](COMPLETION_REPORT.md), [EXECUTIVE_SUMMARY.txt](EXECUTIVE_SUMMARY.txt), [FINALIZATION_SUMMARY.md](FINALIZATION_SUMMARY.md) and [QUICK_REFERENCE.md](QUICK_REFERENCE.md) include **historical** claims such as "production-ready secret management" or "all integration tests complete" that do not describe today's verified scope. They should be treated as archival project notes. **This audit, the current [README](README.md), [SECURITY.md](SECURITY.md), and [RELIABILITY.md](RELIABILITY.md) take precedence.**

## 5. Release criteria / next actions

**Before network-accessible deployment:** address SEC-01 and SEC-02, then SEC-03–SEC-07; require API authorization and recovery tests, external configuration validation, credential rotation review, and secret-history assessment. Do not treat passing CI as approval to deploy.

**Before reliability claims beyond this demo:** complete REL-01–REL-05 and OPS-01, test actual outbox exhaustion and multi-service crash paths, and implement alerts and operator access controls.

**Portfolio-safe claim:** *"Implemented and tested a four-stage Spring Boot/Kafka order choreography with per-service transactional outboxes, at-least-once event publication, database-backed idempotency, a verified Docker Compose happy path, targeted concurrency and replay tests, and documented production/security gaps."*

**Do not claim:** PCI readiness, production-grade authentication, end-to-end exactly-once processing, operational readiness of Vault/Eureka/Config Server, or exhaustive failure recovery.
