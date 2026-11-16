# Current release gates (audit: 16 November 2026)

**Production status: BLOCKED.** Current implementation is a verified local Kafka architecture demonstration, not a certified secure deployment. See [ARCHITECTURE_SECURITY_AUDIT.md](ARCHITECTURE_SECURITY_AUDIT.md) and [README.md](README.md).

- [ ] **P0:** Review and rotate historically exposed credentials ever used outside throwaway development; remove fixed credentials and development Vault root-token assumptions from shared/public deployment paths
- [ ] **P0:** Add authenticated, per-user/per-role authorization to order, inventory, shipping and gateway routes; negative authorization tests; never expose `core-demo`
- [ ] **P1:** Make password change/reset invalidate previously issued JWTs; add recovery throttling, verified-email enrollment and SMTP tests
- [ ] **P1:** Enforce order-status transitions under concurrency; validate prices and ownership server-side
- [ ] **P1:** Test all downstream failure/duplicate/crash points and real outbox retry exhaustion; add monitoring and operator access controls
- [ ] **P1:** Validate Eureka/Config Server/Vault end to end and protect actuator/management interfaces
- [ ] **P2:** Normalize shipping event types and test replay for all DLT topics

The checklist below tracks implemented components and earlier evidence; it must **not** be interpreted as overriding these release gates.

---

# Improvement tracker

Status below distinguishes implemented features **verified by automated tests** from remaining hardening tasks. This project is not production-ready.

## Implemented and verified

- [x] P0 Replace unauthenticated password change with single-use email reset tokens; disable insecure mobile recovery and password logging (**code and unit tests**, not production SMTP validation)
- [x] P0 Green multi-module Gradle build and Continuous Build workflow
- [x] P0 Order outbox broker acknowledgment, bounded retries, backoff and database poller locks
- [x] P0 Route DLT publishing to partition 0 for single-partition dead-letter topics
- [x] P1 Persist payment, inventory and shipping business state, processed-event record, and outgoing outbox event within one local transaction
- [x] P1 Docker-backed order → payment → inventory → shipping E2E, persisted rows and eventual `SHIPPED` state
- [x] P1 Verify four published outbox records, original-event replay deduplication and Kafka outage/restart recovery for a pending order
- [x] P1 Pessimistic product locks for stock reservation (functional test, not concurrency stress test)
- [x] P1 Order status listeners from downstream results
- [x] P1 Move shipping and notification properties into packaged resource locations
- [x] P2 Remove tracked Terraform state and provider artifacts from current tree
- [x] P2 Document verified core setup separately from the larger optional Compose
- [x] P2 Refresh README and security limitations

Core E2E successful CI run: https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43802446235

## Remaining work

- [ ] P0 Rotate any exposed real-world keys and passwords from repository history and invalidate active JWTs
- [x] P0 Verify gateway route in isolated Compose with Eureka/Config Server disabled
- [ ] P0 Verify gateway discovery, Config Server integration and authentication policy for non-demo deployments
- [x] P1 Verify real payment-events.DLT publication, audited one-time replay and shipment recovery
- [ ] P1 Test all downstream crash points and multi-instance outbox polling
- [x] P1 Test 12 concurrent orders with three inventory-service replicas against 4 stock units
- [ ] P1 Repeat higher-volume multi-product and duplicate-delivery stress tests
- [x] P1 Add audited, bounded, explicit manual replay for terminal `FAILED` outbox rows (simulated exhaustion tested)
- [ ] P1 Verify actual eight-attempt exhaustion, metrics/alerts and incident playbooks
- [ ] P1 Review stock shortfall/failed payment compensation and order-state transitions under out-of-order events
- [ ] P2 Rate-limit and audit password-reset requests; verify real SMTP and password-reset UI
- [ ] P2 Validate full observability, secrets injection, Terraform and optional monitoring stack
- [ ] P2 Replace mock payment and simulated notification delivery before claiming real integrations

The four outboxes offer **at-least-once delivery**, not cross-service exactly-once guarantees.

Reliability E2E CI evidence: https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43808339924
