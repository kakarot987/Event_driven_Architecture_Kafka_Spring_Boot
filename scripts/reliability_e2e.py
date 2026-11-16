#!/usr/bin/env python3
"""Real Docker-backed reliability checks: gateway routing, competing inventory
orders, audited FAILED-outbox recovery, and Kafka DLT replay. Operates ONLY on
the disposable core Compose test environment, not production.
"""
import concurrent.futures
import json
import subprocess
import sys
import time
import urllib.error
import urllib.request
import uuid

from core_e2e import command, sql, scalar, request, eventually, wait_for_core_consumers
from replay_dlt import load


def send_order(product, quantity=1):
    return request("POST", "/api/orders", {
        "userId": "reliability-test-user", "totalAmount": 7.50 * quantity,
        "items": [{"productId": product, "quantity": quantity, "price": 7.50}]
    })["orderId"]


def seed(product, quantity):
    sql("INSERT INTO inventory_db.products "
        "(product_id,available_quantity,created_at,updated_at) "
        f"VALUES ('{product}',{quantity},NOW(),NOW());")


def field(db, table, column, order):
    return scalar(f"SELECT {column} FROM {db}.{table} WHERE order_id='{order}' LIMIT 1;")


def gateway_test():
    # Proves the real WebFlux gateway forwarded to the order service in Compose.
    url = "http://127.0.0.1:8080/api/orders/health"
    def healthy():
        with urllib.request.urlopen(url, timeout=6) as response:
            return "running" in response.read().decode("utf8")
    eventually("gateway actually forwards /api/orders/health", healthy, timeout=180)
    # A non-configured path must not be forwarded to the order service.
    try:
        urllib.request.urlopen("http://127.0.0.1:8080/not-an-order-route", timeout=6)
    except urllib.error.HTTPError as error:
        assert error.code == 404, f"Unexpected gateway status: {error.code}"
    else:
        raise AssertionError("Unknown route should have returned 404")
    print("PASS: gateway reachable, real order route works, unknown route rejects")


def concurrent_reservations():
    product = "COMPETE-" + uuid.uuid4().hex[:12]
    seed(product, 4)
    # Submitted concurrently. Kafka spreads order IDs across partitions, and
    # the CI overlay scales inventory-service to multiple consumer instances.
    with concurrent.futures.ThreadPoolExecutor(max_workers=12) as executor:
        orders = list(executor.map(lambda _: send_order(product), range(12)))
    assert len(orders) == len(set(orders)) == 12
    results = {}
    for order in orders:
        results[order] = eventually(f"competing order {order} terminal",
            lambda order=order: (state if (state := request(
                "GET", f"/api/orders/{order}").get("status")) in ("SHIPPED", "FAILED") else None),
            timeout=180)
    success = [order for order, status in results.items() if status == "SHIPPED"]
    failures = [order for order, status in results.items() if status == "FAILED"]
    assert len(success) == 4 and len(failures) == 8, results
    assert scalar("SELECT available_quantity FROM inventory_db.products "
                  f"WHERE product_id='{product}';") == "0"
    assert scalar("SELECT COUNT(*) FROM inventory_db.inventory_reservations "
                  f"WHERE product_id='{product}';") == "4"
    assert scalar("SELECT COALESCE(SUM(quantity),0) FROM inventory_db.inventory_reservations "
                  f"WHERE product_id='{product}';") == "4"
    assert scalar("SELECT COUNT(DISTINCT order_id) FROM inventory_db.inventory_reservations "
                  f"WHERE product_id='{product}';") == "4"
    print("PASS: 12 concurrent orders, 4 reserved/shipped, 8 failed, no oversell", flush=True)


def failed_outbox_recovery():
    product = "REQUEUE-" + uuid.uuid4().hex[:12]
    seed(product, 1)
    print("Stopping Kafka for a controlled terminal-outbox recovery test", flush=True)
    command("stop", "kafka")
    try:
        order = send_order(product)
        eventually("outbox pending while Kafka offline", lambda:
            scalar("SELECT COUNT(*) FROM order_db.outbox_events "
                   f"WHERE aggregate_id='{order}' AND publish_status='PENDING';") == "1", timeout=25)
        # Deliberate simulation of the 8-attempt terminal state; production
        # exhaustion/retry logic is covered separately by outbox tests.
        sql("UPDATE order_db.outbox_events SET publish_status='FAILED',attempts=8 "
            f"WHERE aggregate_id='{order}' AND publish_status='PENDING';")
        assert scalar("SELECT COUNT(*) FROM order_db.outbox_events "
                      f"WHERE aggregate_id='{order}' AND publish_status='FAILED';") == "1"
    finally:
        command("start", "kafka")
    eventually("Kafka restored", lambda:
        "kafka" in command("ps", "--services", "--status", "running"), timeout=90)
    event_id = int(scalar("SELECT id FROM order_db.outbox_events "
                          f"WHERE aggregate_id='{order}';"))
    subprocess.run([sys.executable, "scripts/replay_failed_outbox.py", "--service", "order",
                    "--id", str(event_id), "--operator", "ci-operator", "--confirm"], check=True)
    eventually("requeued order eventually shipped", lambda:
        request("GET", f"/api/orders/{order}").get("status") == "SHIPPED", timeout=180)
    assert scalar("SELECT COUNT(*) FROM order_db.outbox_replay_audit "
                  f"WHERE outbox_id={event_id} AND operator_id='ci-operator';") == "1"
    assert scalar("SELECT publish_status FROM order_db.outbox_events "
                  f"WHERE id={event_id};") == "PUBLISHED"
    attempt_again = subprocess.run([sys.executable, "scripts/replay_failed_outbox.py",
                    "--service", "order", "--id", str(event_id),
                    "--operator", "ci-operator", "--confirm"], capture_output=True)
    assert attempt_again.returncode != 0, "A PUBLISHED event must not requeue"
    print("PASS: simulated terminal FAILED outbox was audited, requeued once and published", flush=True)


def dead_letter_replay():
    product = "DLT-PROBE-" + uuid.uuid4().hex[:12]
    seed(product, 1)
    order = send_order(product)
    eventually("payment published for failure injection", lambda:
        scalar("SELECT COUNT(*) FROM payment_db.outbox_events "
               f"WHERE aggregate_id='{order}' AND publish_status='PUBLISHED';") == "1")
    payment_payload = json.loads(scalar(
        "SELECT payload FROM payment_db.outbox_events "
        f"WHERE aggregate_id='{order}' LIMIT 1;"))
    expected_event_id = payment_payload["eventId"]

    # DLT partition 0 is the destination for failures from ANY source partition.
    eventually("controlled failure event delivered to payment-events.DLT", lambda:
        (record if (record := get_probe_dlt()) and
         record[2].get("eventId") == expected_event_id else None), timeout=120, interval=3)
    assert scalar("SELECT COUNT(*) FROM inventory_db.inventory_reservations "
                  f"WHERE order_id='{order}';") == "0"
    args=[sys.executable, "scripts/replay_dlt.py", "--source-topic", "payment-events",
          "--offset", "0", "--event-id", expected_event_id,
          "--operator", "ci-operator", "--confirm"]
    subprocess.run(args, check=True)
    eventually("DLT replay eventually shipped", lambda:
        request("GET", f"/api/orders/{order}").get("status") == "SHIPPED", timeout=180)
    assert scalar("SELECT COUNT(*) FROM inventory_db.inventory_reservations "
                  f"WHERE order_id='{order}';") == "1"
    assert scalar("SELECT available_quantity FROM inventory_db.products "
                  f"WHERE product_id='{product}';") == "0"
    assert scalar("SELECT COUNT(*) FROM order_db.dlt_replay_audit "
                  "WHERE source_topic='payment-events' AND source_offset=0 "
                  "AND status='PUBLISHED';") == "1"
    assert subprocess.run(args, capture_output=True).returncode != 0, (
        "second replay of the same dead-letter offset must be rejected")
    print("PASS: real DLT failure, preserved-ID replay, one-time audit and shipment", flush=True)


def get_probe_dlt():
    try:
        return load("payment-events", 0)
    except (RuntimeError, ValueError, json.JSONDecodeError):
        return None


def main():
    eventually("order HTTP ready", lambda:
        "running" in request("GET", "/api/orders/health"), timeout=180)
    wait_for_core_consumers()
    gateway_test()
    concurrent_reservations()
    failed_outbox_recovery()
    dead_letter_replay()
    print("PASS: gateway, concurrent stock, terminal outbox recovery and DLT replay", flush=True)


if __name__ == "__main__":
    main()
