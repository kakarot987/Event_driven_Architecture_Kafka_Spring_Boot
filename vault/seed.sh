#!/bin/bash
set -e

echo "Waiting for Vault container to be healthy..."
until docker compose exec -T vault sh -c "VAULT_ADDR=http://127.0.0.1:8200 vault status" > /dev/null 2>&1; do
  echo "Waiting for Vault..."
  sleep 1
done

echo "Vault is up. Seeding secrets..."

docker compose exec -T vault sh -c "
  export VAULT_ADDR=http://127.0.0.1:8200
  export VAULT_TOKEN=root
  vault secrets enable -path=secret kv-v2 || true

  vault kv put secret/eureka-server \
    EUREKA_USR=eureka_admin \
    EUREKA_PWD=eureka_pass_local

  vault kv put secret/eureka-server/docker \
    spring.application.name=eureka-server \
    server.port=8761

  vault kv put secret/auth-service \
    DB_USERNAME=auth_user \
    DB_PASSWORD=auth_pass_local \
    JWT_SECRET=dev-only-secret \
    EUREKA_USR=eureka_admin \
    EUREKA_PWD=eureka_pass_local

  vault kv put secret/order-service \
    DB_USERNAME=order_user \
    DB_PASSWORD=order_pass_local \
    PAYMENT_API_KEY=dev-fake-key \
    EUREKA_USR=eureka_admin \
    EUREKA_PWD=eureka_pass_local
"

echo "Vault seeded with dev secrets."