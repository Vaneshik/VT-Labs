param(
    [int]$Port = 8079,
    [int]$Config = 1
)

$ErrorActionPreference = "Stop"
$url = "http://localhost:$Port/?token=518506673&user=-1355007012&config=$Config"

Write-Host "Checking tunnel: $url"

try {
    $response = Invoke-WebRequest -Uri $url -Method GET -TimeoutSec 15 -UseBasicParsing
    Write-Host "HTTP status: $($response.StatusCode)"
    if ($response.StatusCode -eq 403) {
        throw "Server returned HTTP 403. Check token, user, config, and URL parameters."
    }
    Write-Host "Tunnel check passed."
} catch {
    Write-Error "Tunnel check failed. Start SSH forwarding first: ssh -N -L $Port`:stload.se.ifmo.ru`:8080 s409858@se.ifmo.ru -p 2222"
    throw
}

