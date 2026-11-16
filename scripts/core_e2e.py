#!/usr/bin/env python3
"""Live, black-box choreography smoke test. Requires the 6-service compose.core.yml stack.
Usage: DB_PASSWORD=... python3 scripts/core_e2e.py
Requires Python 3; no pip dependencies. Starts with pre-existing running containers.
"""
import json
import os
import re
import subprocess
import sys
import time
import urllib.error
import urllib.request
import uuid

COMPOSE = ["docker", "compose", "-f", "compose.core.yml"]
BASE = "http://127.0.0.1:8081"
PASSWORD = os.environ.get("DB_PASSWORD")
if not PASSWORD:
    sys.exit("DB_PASSWORD must be set; never commit it.")
deadline_seconds = int(os.environ.get("E2E_TIMEOUT_SECONDS", "240"))


def command(*args, input=None):
    result = subprocess.run([*COMPOSE, *args], text=True, input=input,
                            capture_output=True, timeout=60)
    if result.returncode:
        raise RuntimeError(f"Command failed: {args[:3]!r}\n{result.stderr[-1200:]}")
    return result.stdout.strip()


def sql(statement):
    # All identifiers/values come from fixed literals or strict UUID/token validation.
    return command("exec", "-T", "-e", "MYSQL_PWD=" + PASSWORD,
                   "mysql", "mysql", "-uroot", "--batch", "--skip-column-names",
                   "-e", statement)


def scalar(statement):
    result = sql(statement)
    return result.splitlines()[-1].strip() if result else ""


def eventually(label, probe, timeout=deadline_seconds, interval=2):
    deadline = time.monotonic() + timeout
    last = None
    while time.monotonic() < deadline:
        try:
            value = probe()
            if value:
                print(f"OK: {label}", flush=True)
                return value
        except (RuntimeError, OSError, urllib.error.URLError) as exc:
            last = str(exc)
        time.sleep(interval)
    raise AssertionError(f"Timed out waiting for {label}; last error={last}")


def request(method, path, data=None):
    body = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(BASE + path, data=body, method=method,
                                 headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=8) as response:
        return json.load(response) if path != "/api/orders/health" else response.read().decode()


def run_order(label):
    unique = uuid.uuid4().hex[:12]
    product = "E2E-" + unique
    # Hibernate owns schema creation; wait for the actual products table first.
    eventually("inventory schema", lambda: scalar(
        "SELECT COUNT(*) FROM information_schema.tables "
        "WHERE table_schema='inventory_db' AND table_name='products';") == "1")
    sql("INSERT INTO inventory_db.products "
        "(product_id,available_quantity,created_at,updated_at) "
        f"VALUES ('{product}',5,NOW(),NOW());")

    created = request("POST", "/api/orders", {
        "userId": "e2e-test-user",
        "totalAmount": 15.00,
        "items": [{"productId": product, "quantity": 2, "price": 7.50}]
    })
    order_id = created["orderId"]
    assert re.fullmatch(r"[0-9a-f-]{36}", order_id), created
    print(f"{label}: created order {order_id}", flush=True)
    eventually(f"{label}: shipped", lambda:
               request("GET", f"/api/orders/{order_id}").get("status") == "SHIPPED")

    def count(database, table, clause=""):
        return int(scalar(f"SELECT COUNT(*) FROM {database}.{table} "
                          f"WHERE order_id='{order_id}'{clause};"))

    eventually(f"{label}: payment recorded", lambda:
               count("payment_db", "payments", " AND status='SUCCESS'") == 1)
    eventually(f"{label}: inventory reservation recorded", lambda:
               count("inventory_db", "inventory_reservations") == 1)
    eventually(f"{label}: shipment recorded", lambda:
               count("shipping_db", "shipments", " AND status='CREATED'") == 1)

    for db in ("order_db", "payment_db", "inventory_db", "shipping_db"):
        eventually(f"{label}: {db} outbox published", lambda db=db:
                   scalar(f"SELECT COUNT(*) FROM {db}.outbox_events "
                          f"WHERE aggregate_id='{order_id}' AND publish_status='PUBLISHED';") == "1")

    assert scalar("SELECT available_quantity FROM inventory_db.products "
                  f"WHERE product_id='{product}';") == "3", "Stock was not decremented exactly twice"
    assert count("payment_db", "payments") == 1
    assert count("shipping_db", "shipments") == 1
    assert count("inventory_db", "inventory_reservations") == 1
    return order_id, product


def replay_order_event(order_id, product):
    original = scalar("SELECT payload FROM order_db.outbox_events "
                      f"WHERE aggregate_id='{order_id}' AND event_type='OrderPlacedEvent';")
    assert json.loads(original)["orderId"] == order_id
    command("exec", "-T", "kafka", "/opt/kafka/bin/kafka-console-producer.sh",
            "--bootstrap-server", "kafka:9092", "--topic", "order-events",
            "--property", "parse.key=true", "--property", "key.separator=:",
            input=f"{order_id}:{original}\n")
    # Give the consumer time to see the duplicate, then prove no duplicate side-effects.
    time.sleep(8)
    for database, table in (("payment_db", "payments"),
                            ("inventory_db", "inventory_reservations"),
                            ("shipping_db", "shipments")):
        assert scalar(f"SELECT COUNT(*) FROM {database}.{table} "
                      f"WHERE order_id='{order_id}';") == "1", f"duplicate {table}"
    assert scalar("SELECT available_quantity FROM inventory_db.products "
                  f"WHERE product_id='{product}';") == "3", "replay double-decremented stock"
    print("OK: replay did not create duplicate business state", flush=True)


def main():
    eventually("order HTTP ready", lambda: "running" in request("GET", "/api/orders/health"))
    first, product = run_order("happy path")
    replay_order_event(first, product)

    # Recover a pending order event across a broker outage, without losing DB intent.
    print("Stopping broker to test pending outbox recovery", flush=True)
    command("stop", "kafka")
    try:
        second, _ = create_order_while_broker_down()
    finally:
        command("start", "kafka")
    eventually("broker restarted", lambda: "kafka" in command("ps", "--services", "--status", "running"))
    eventually("broker-outage order eventually shipped",
               lambda: request("GET", f"/api/orders/{second}").get("status") == "SHIPPED")
    print("PASS: end-to-end, duplicate replay, and broker outage recovery", flush=True)


def create_order_while_broker_down():
    product = "E2E-" + uuid.uuid4().hex[:12]
    sql("INSERT INTO inventory_db.products (product_id,available_quantity,created_at,updated_at) "
        f"VALUES ('{product}',5,NOW(),NOW());")
    order_id = request("POST", "/api/orders", {
        "userId": "e2e-test-user", "totalAmount": 9.00,
        "items": [{"productId": product, "quantity": 1, "price": 9.00}]
    })["orderId"]
    eventually("order persisted while broker offline", lambda:
               scalar("SELECT COUNT(*) FROM order_db.outbox_events "
                      f"WHERE aggregate_id='{order_id}' AND publish_status='PENDING';") == "1",
               timeout=30)
    return order_id, product


if __name__ == "__main__":
    main()
