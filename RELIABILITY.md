# Gateway and Kafka reliability verification

**Verified in GitHub Actions on November 16, 2026:** https://github.com/kakarot987/Event_driven_Architecture_Kafka_Spring_Boot/actions/runs/43808339924

The disposable core Compose stack passed four live scenarios:

1. **Gateway:** WebFlux forwarded `/api/orders/health` to the order service and returned HTTP 404 for an unknown path. Config Server and Eureka were **disabled** in this test; discovery integration still requires testing.
2. **Concurrent stock:** Three inventory-service replicas processed 12 competing orders against four available units. Exactly four shipped, eight failed, stock reached zero, and exactly four reservations existed.
3. **Terminal outbox:** A terminal `FAILED` row was **simulated** while Kafka was offline, then manually requeued with an audit entry. The original event was published and the order shipped. Actual eight-attempt retry exhaustion was not tested.
4. **Dead-letter replay:** A controlled failure in the `reliability-test` profile produced a real `payment-events.DLT` record. The operator CLI replayed original topic partition 0 offset 0 while preserving the Kafka key and event ID, and shipment succeeded. A second replay was rejected by a database unique constraint.

## Reproduction

The full reproducible sequence is in `.github/workflows/ReliabilityE2E.yml`. It builds the real images, starts disposable MySQL/Kafka, pre-creates the four source topics and their one-partition DLTs, starts Gateway and three inventory replicas, and runs `python3 -u scripts/reliability_e2e.py`.

Do not use `core-demo` or `reliability-test` profiles on shared or production systems. The former permits test-only unauthenticated order creation; the latter intentionally injects failures.

## Manual terminal outbox replay

On the isolated core stack, with `DB_PASSWORD` supplied via environment and the `02-replay-audit.sql` schema applied:

```sh
python3 scripts/replay_failed_outbox.py \
  --service order --id 12 --operator operator-123 --confirm
```

Only a `FAILED` event can be requeued. The CLI preserves payload and event ID, resets retry counters, and records the operator label transactionally in the same MySQL transaction. The limit is three manual requeues per outbox row. A replay after publication is rejected. The operator label is an **audit label, not authentication**.

## Manual dead-letter replay

Review the original Kafka event type, offset, key, payload and downstream effects before running:

```sh
python3 scripts/replay_dlt.py \
  --source-topic payment-events --offset 0 \
  --event-id <actual-event-uuid> \
  --operator operator-123 --confirm
```

The CLI only accepts core topics and an exact DLT offset + expected event ID. A unique audit claim is stored **before** publishing. It sends the original event identity to its source topic. Duplicate replay of the same DLT offset is rejected.

If DLT publication is uncertain, audit status remains `CLAIMED`: inspect Kafka and downstream records manually rather than blindly replaying again. Both outbox recovery and DLT replay retain **at-least-once** delivery semantics, not exactly-once guarantees.

## Remaining boundaries

The proof does not cover Config Server/Eureka integration, every downstream crash window, repeated heavy-load stress runs, or actual outbox exhaustion through eight failed sends. Production tooling would require authenticated operator access, durable schema migrations, metrics/alerts and incident approvals.
