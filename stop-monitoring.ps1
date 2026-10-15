Write-Host "Stopping monitoring services..."

docker compose stop `
    elasticsearch `
    logstash `
    kibana `
    prometheus `
    grafana

Write-Host ""
Write-Host "Monitoring services stopped."
Write-Host ""

docker compose ps