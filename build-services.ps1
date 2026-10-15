$services = @(
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

foreach ($service in $services) {
    Write-Host ""
    Write-Host "========================================"
    Write-Host "Building $service"
    Write-Host "========================================"

    docker compose build $service --progress=plain

    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "Build failed for: $service"
        exit $LASTEXITCODE
    }

    Write-Host "Build successful: $service"
}

Write-Host ""
Write-Host "All services built successfully."