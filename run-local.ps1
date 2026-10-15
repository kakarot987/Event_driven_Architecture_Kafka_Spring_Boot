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
    "notification-service"
)

Write-Host ""
Write-Host "Starting local development stack..."
Write-Host ""

docker compose up -d $services

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start local stack."
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Local stack started."
Write-Host ""

docker compose ps