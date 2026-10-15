$services = @(
    "mysql",
    "kafka",
    "vault",
    "vault-seed",

    "config-server",
    "eureka-server",
    "gateway",

    "authentication-authorization",
    "order-service",
    "payment-service",
    "inventory-service",
    "shipping-service",
    "notification-service",

    "elasticsearch",
    "logstash",
    "kibana",
    "prometheus",
    "grafana"
)

Write-Host ""
Write-Host "Starting full application + monitoring stack..."
Write-Host ""

docker compose up -d $services

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start monitoring stack."
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Full monitoring stack started."
Write-Host ""

docker compose ps