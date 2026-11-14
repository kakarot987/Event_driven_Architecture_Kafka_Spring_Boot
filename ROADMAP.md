# Improvement tracker

Status reflects **code present on the hardening branch**, not verified runtime behavior. Nothing below should be marketed as production-ready without independent testing.

- [x] P0 Replace email password reset with 15-minute, hashed single-use tokens; disable legacy mobile password reset; remove password/OTP logging
- [ ] P0 Rotate exposed secrets outside this repository and audit leaked Git history
- [ ] P0 Confirm green full multi-module Gradle build and Continuous Build workflow
- [x] P0 Await Kafka acknowledgment before marking order outbox events published; backoff, bounded retries, row locking, FAILED status
- [x] P0 Send DLT recoveries to partition 0
- [ ] P0 Verify Gateway startup, route to order-service and Config Server overrides (configuration updated)
- [x] P1 Move shipping and notification properties into Gradle resources
- [ ] P1 Full order → payment → inventory → shipping integration test, including eventual state
- [ ] P1 Crash/replay/DLT integration tests with running Kafka and MySQL
- [x] P1 Introduce pessimistic product row locks to prevent oversell
- [x] P1 Implement order status listeners for payment, inventory and shipping topics (requires integration verification)
- [x] P2 Remove committed Terraform state/provider cache from current tree
- [x] P2 Replace minimal README and document real known limitations
- [x] P2 Separate minimal local Compose from optional monitoring stack (runtime verification outstanding)

## Further reliability and security tasks

- Extend transactional outbox and inbox pattern to payment, inventory and shipping, including unique order/business keys, outbox replay, and concurrency tests
- Add email reset request throttling, delivery monitoring, verified-email enrollment, and JWT invalidation strategy
- Add secure secret injection to all service and infrastructure config (Compose remains a development scaffold)
- Verify MySQL schema initialization, Kafka topic creation, Prometheus actuator endpoints, gateway discovery and CI on a clean runner
- Reconcile docs/reports with actual tests and operational evidence
