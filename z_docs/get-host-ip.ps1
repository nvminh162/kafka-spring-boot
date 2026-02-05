# Script to get host IP address for Windows
# Usage: .\get-host-ip.ps1
#        .\get-host-ip.ps1 -UpdateEnvFile  (to automatically update local.env)

param(
    [switch]$UpdateEnvFile
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Host IP Address Detection Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get all IPv4 addresses, excluding loopback, APIPA, Docker networks
$ipAddresses = Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.IPAddress -notlike '127.*' -and 
    $_.IPAddress -notlike '169.254.*' -and
    $_.IPAddress -notlike '172.17.*' -and
    $_.IPAddress -notlike '172.18.*' -and
    $_.IPAddress -notlike '172.19.*' -and
    $_.IPAddress -notlike '172.20.*' -and
    $_.IPAddress -notlike '172.21.*' -and
    $_.IPAddress -notlike '172.22.*' -and
    $_.IPAddress -notlike '172.23.*' -and
    $_.IPAddress -notlike '172.24.*' -and
    $_.IPAddress -notlike '172.25.*' -and
    $_.IPAddress -notlike '172.26.*' -and
    $_.IPAddress -notlike '172.27.*' -and
    $_.IPAddress -notlike '172.28.*' -and
    $_.IPAddress -notlike '172.29.*' -and
    $_.IPAddress -notlike '172.30.*' -and
    $_.IPAddress -notlike '172.31.*' -and
    $_.InterfaceAlias -notlike '*Loopback*' -and
    $_.InterfaceAlias -notlike '*Docker*'
} | Select-Object IPAddress, InterfaceAlias

if ($ipAddresses.Count -eq 0) {
    Write-Host "ERROR: Could not find any valid IP address!" -ForegroundColor Red
    Write-Host "Using fallback: 127.0.0.1" -ForegroundColor Yellow
    $ipAddress = "127.0.0.1"
} elseif ($ipAddresses.Count -eq 1) {
    $ipAddress = $ipAddresses[0].IPAddress
    Write-Host "Found IP Address: $ipAddress" -ForegroundColor Green
    Write-Host "Interface: $($ipAddresses[0].InterfaceAlias)" -ForegroundColor Gray
} else {
    Write-Host "Multiple IP addresses found:" -ForegroundColor Yellow
    Write-Host ""
    $index = 1
    foreach ($ip in $ipAddresses) {
        Write-Host "  [$index] $($ip.IPAddress) - $($ip.InterfaceAlias)" -ForegroundColor Cyan
        $index++
    }
    Write-Host ""
    $selected = Read-Host "Select IP address (1-$($ipAddresses.Count))"
    
    if ($selected -match '^\d+$' -and [int]$selected -ge 1 -and [int]$selected -le $ipAddresses.Count) {
        $ipAddress = $ipAddresses[[int]$selected - 1].IPAddress
        Write-Host "Selected: $ipAddress" -ForegroundColor Green
    } else {
        Write-Host "Invalid selection. Using first IP: $($ipAddresses[0].IPAddress)" -ForegroundColor Yellow
        $ipAddress = $ipAddresses[0].IPAddress
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Result: $ipAddress" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Update local.env file if requested
if ($UpdateEnvFile) {
    $envFile = "local.env"
    if (Test-Path $envFile) {
        $content = "HOST_IP_ADDRESS=$ipAddress"
        Set-Content -Path $envFile -Value $content
        Write-Host "✓ Updated $envFile with HOST_IP_ADDRESS=$ipAddress" -ForegroundColor Green
    } else {
        Write-Host "✗ File $envFile not found. Creating new file..." -ForegroundColor Yellow
        $content = "HOST_IP_ADDRESS=$ipAddress"
        Set-Content -Path $envFile -Value $content
        Write-Host "✓ Created $envFile with HOST_IP_ADDRESS=$ipAddress" -ForegroundColor Green
    }
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "  1. Restart Docker containers: docker-compose down && docker-compose up -d" -ForegroundColor White
} else {
    Write-Host "To automatically update local.env, run:" -ForegroundColor Yellow
    Write-Host "  .\get-host-ip.ps1 -UpdateEnvFile" -ForegroundColor White
    Write-Host ""
    Write-Host "Or manually update local.env with:" -ForegroundColor Yellow
    Write-Host "  HOST_IP_ADDRESS=$ipAddress" -ForegroundColor White
}

return $ipAddress
