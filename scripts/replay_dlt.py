#!/usr/bin/env python3
"""Audited, operator-confirmed single DLT record replay.
Reproduces an original Kafka event without replacing its identity. Requires a
precise source offset, matching event ID, and explicit confirmation.

Usage:
  python3 scripts/replay_dlt.py --source-topic payment-events --offset 0 \
    --event-id <actual-event-id> --operator demo-operator --confirm

A claimed record is never automatically replayed again. If publication fails,
its audit status remains CLAIMED for explicit manual incident review. The
original Kafka event ID and payload are retained, so consumer idempotency works.
"""
import argparse
import json
import os
import re
import subprocess
import sys

TOPICS = {
    "order-events": "OrderPlacedEvent",
    "payment-events": "PaymentProcessedEvent",
    "inventory-events": "InventoryReservedEvent",
    "shipping-events": "ShippingCreatedEvent",
}
SAFE = re.compile(r"[A-Za-z0-9._@-]{2,100}\Z")
EVENT_ID = re.compile(r"[A-Za-z0-9_-]{8,128}\Z")


def compose(*args, input=None, timeout=40):
    res = subprocess.run(["docker", "compose", "-f", "compose.core.yml", *args],
                         capture_output=True, input=input, text=True, timeout=timeout)
    if res.returncode:
        raise RuntimeError((res.stderr or res.stdout)[-1000:])
    return res.stdout


def db(statement):
    password = os.environ.get("DB_PASSWORD")
    if not password:
        raise RuntimeError("DB_PASSWORD must be set")
    env = os.environ.copy()
    env["MYSQL_PWD"] = password
    res = subprocess.run(
        ["docker", "compose", "-f", "compose.core.yml", "exec", "-T",
         "-e", "MYSQL_PWD", "mysql", "mysql", "-uroot",
         "--batch", "--skip-column-names", "-e", statement],
        capture_output=True, text=True, env=env, timeout=30)
    if res.returncode:
        raise RuntimeError(res.stderr[-800:])
    return res.stdout.strip()


def load(source, offset):
    out = compose("exec", "-T", "kafka",
                  "/opt/kafka/bin/kafka-console-consumer.sh",
                  "--bootstrap-server", "kafka:9092",
                  "--topic", source + ".DLT",
                  "--partition", "0", "--offset", str(offset),
                  "--max-messages", "1", "--timeout-ms", "8000",
                  "--property", "print.key=true",
                  "--property", "key.separator=:",
                  timeout=20)
    # Kafka's console consumer may emit a message-count or warning line in
    # addition to the record. Locate a validated JSON record instead of
    # assuming stdout consists of exactly one line.
    for line in out.splitlines():
        if ":" not in line:
            continue
        key, raw = line.split(":", 1)
        if not re.fullmatch(r"[A-Za-z0-9_-]{8,128}", key):
            continue
        try:
            event = json.loads(raw)
        except json.JSONDecodeError:
            continue
        if isinstance(event, dict) and event.get("eventId"):
            return key, raw, event
    raise RuntimeError("DLT record not found in Kafka consumer output: " + repr(out[:240]))


def replay(source, offset, expected_event_id, operator):
    key, raw, event = load(source, offset)
    if event.get("eventType") != TOPICS[source] or event.get("eventId") != expected_event_id:
        raise RuntimeError("Event type or event ID differs from operator's exact expected values")
    if key != event.get("orderId") or not key or not re.fullmatch(r"[A-Za-z0-9_-]{8,128}", key):
        raise RuntimeError("Kafka key does not match event orderId")
    # Unique constraint on original topic / partition / offset prevents accidental double replay.
    db(f"""INSERT INTO order_db.dlt_replay_audit
        (source_topic,source_partition,source_offset,event_id,operator_id,status)
        VALUES ('{source}',0,{offset},'{expected_event_id}','{operator}','CLAIMED');""")
    try:
        compose("exec", "-T", "kafka",
                "/opt/kafka/bin/kafka-console-producer.sh",
                "--bootstrap-server", "kafka:9092",
                "--topic", source,
                "--producer-property", "acks=all",
                "--producer-property", "delivery.timeout.ms=15000",
                "--property", "parse.key=true",
                "--property", "key.separator=:",
                input=key + ":" + raw + "\n")
    except Exception:
        # CLAIMED is deliberate. Operator must inspect Kafka before changing it.
        raise RuntimeError("DLT replay send uncertain: audit remains CLAIMED; inspect broker and "
                           "consumer state before intervention")
    db(f"""UPDATE order_db.dlt_replay_audit
        SET status='PUBLISHED',published_at=NOW()
        WHERE source_topic='{source}' AND source_partition=0 AND source_offset={offset}
        AND status='CLAIMED';""")
    print(f"DLT offset {offset} from {source}.DLT replayed to {source}; "
          f"event identity retained, operator={operator}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-topic", required=True, choices=TOPICS)
    parser.add_argument("--offset", type=int, required=True)
    parser.add_argument("--event-id", required=True)
    parser.add_argument("--operator", required=True)
    parser.add_argument("--confirm", action="store_true")
    args = parser.parse_args()
    if args.offset < 0 or not EVENT_ID.fullmatch(args.event_id) or not SAFE.fullmatch(args.operator):
        parser.error("Invalid offset, event ID or operator identifier")
    if not args.confirm:
        parser.error("DLT replay requires an explicit --confirm")
    replay(args.source_topic, args.offset, args.event_id, args.operator)


if __name__ == "__main__":
    main()
