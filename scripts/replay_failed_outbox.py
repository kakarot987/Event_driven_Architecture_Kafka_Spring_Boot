#!/usr/bin/env python3
"""Manual, deliberately non-automatic replay of a terminal FAILED outbox record.
Requires the disposable local compose.core.yml stack and schema from mysql-init-core.
An operator must supply the service, record id, identity and explicit confirmation.

Usage: python3 scripts/replay_failed_outbox.py --service inventory --id 12 \
    --operator demo-operator --confirm

Preserves the original event payload and event ID. Maximum three manual requeues.
Audit and requeue occur in a single database transaction. Replays can duplicate
already-delivered Kafka records; downstream idempotency remains essential.
"""
import argparse
import os
import re
import subprocess
import sys

DATABASES = dict(order="order_db", payment="payment_db",
                 inventory="inventory_db", shipping="shipping_db")
SAFE_OPERATOR = re.compile(r"[A-Za-z0-9._@-]{2,100}\Z")


def mysql(query):
    password = os.environ.get("DB_PASSWORD")
    if not password:
        raise RuntimeError("Set DB_PASSWORD through local secrets; do not commit credentials")
    env = os.environ.copy()
    env["MYSQL_PWD"] = password
    cmd = ["docker", "compose", "-f", "compose.core.yml", "exec", "-T",
           "-e", "MYSQL_PWD", "mysql", "mysql", "-uroot",
           "--batch", "--skip-column-names", "-e", query]
    result = subprocess.run(cmd, env=env, text=True, capture_output=True, timeout=30)
    if result.returncode:
        raise RuntimeError(result.stderr.strip()[-700:])
    return result.stdout.strip()


def replay(service, event_id, operator):
    db = DATABASES[service]
    # Stable row-level lock prevents multiple operators from racing.
    # ROW_COUNT reflects the conditional status transition; only that same
    # successful transaction inserts an audit event.
    statement = f"""
START TRANSACTION;
UPDATE {db}.outbox_events
SET publish_status='PENDING', attempts=0, next_attempt_at=NULL, published_at=NULL
WHERE id={event_id} AND publish_status='FAILED'
  AND (SELECT COUNT(*) FROM {db}.outbox_replay_audit WHERE outbox_id={event_id}) < 3;
SET @requeued=ROW_COUNT();
INSERT INTO {db}.outbox_replay_audit (outbox_id,operator_id)
SELECT {event_id},'{operator}' FROM DUAL WHERE @requeued=1;
COMMIT;
SELECT @requeued;
"""
    return mysql(statement).splitlines()[-1] == "1"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--service", choices=DATABASES, required=True)
    parser.add_argument("--id", type=int, required=True, help="outbox DB row id")
    parser.add_argument("--operator", required=True, help="auditable operator identifier")
    parser.add_argument("--confirm", action="store_true",
                        help="I reviewed this FAILED event and accept possible redelivery")
    args = parser.parse_args()
    if args.id < 1 or not SAFE_OPERATOR.fullmatch(args.operator):
        parser.error("ID must be positive; operator must be 2-100 safe characters")
    if not args.confirm:
        parser.error("Refusing to replay without --confirm")
    if not replay(args.service, args.id, args.operator):
        sys.exit("No FAILED row was requeued; it may already be pending/published, "
                 "or manual replay limit (3) reached.")
    print(f"Requeued {args.service} outbox id={args.id} with preserved payload; "
          "audit recorded. Monitor PUBLISHED or FAILED status.")


if __name__ == "__main__":
    main()
