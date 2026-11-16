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
- [ ] P0 Verify gateway routing and authentication policy when enabling non-demo infrastructure
- [ ] P1 Test downstream outage windows, multi-instance outbox polling, consumer crash points and DLT replay
- [ ] P1 Add concurrency stress tests for duplicate deliveries and simultaneous inventory reservation
- [ ] P1 Monitor and manually replay terminal `FAILED` outbox rows; introduce alerts and recovery procedures
- [ ] P1 Review stock shortfall/failed payment compensation and order-state transitions under out-of-order events
- [ ] P2 Rate-limit and audit password-reset requests; verify real SMTP and password-reset UI
- [ ] P2 Validate full observability, secrets injection, Terraform and optional monitoring stack
- [ ] P2 Replace mock payment and simulated notification delivery before claiming real integrations

The four outboxes offer **at-least-once delivery**, not cross-service exactly-once guarantees.
