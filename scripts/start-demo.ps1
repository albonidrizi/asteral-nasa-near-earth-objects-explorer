$ErrorActionPreference = "Stop"

$envFile = Join-Path $PSScriptRoot "..\.env"
if (-not (Test-Path $envFile)) {
    $bytes = New-Object byte[] 32
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    $password = [Convert]::ToBase64String($bytes).Replace("+", "_").Replace("/", "-")
    Set-Content -Path $envFile -Value "DB_PASSWORD=$password" -Encoding ascii
    Write-Host "Created .env with a generated local database password."
}

docker compose --env-file $envFile up --build -d --wait
docker compose --env-file $envFile ps
