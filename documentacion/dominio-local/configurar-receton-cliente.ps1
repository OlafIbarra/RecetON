[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ServerIp
)
$ErrorActionPreference = 'Stop'
$hostsPath = Join-Path $env:WINDIR 'System32\drivers\etc\hosts'
$parsed = $null
if (-not [System.Net.IPAddress]::TryParse($ServerIp, [ref]$parsed) -or $parsed.AddressFamily -ne [System.Net.Sockets.AddressFamily]::InterNetwork -or $ServerIp -like '127.*' -or $ServerIp -like '169.254.*') {
    throw 'Indica la IPv4 LAN actual de la laptop servidor, por ejemplo -ServerIp "192.168.1.73".'
}
if (-not (Test-Path -LiteralPath $hostsPath)) { throw "No encontré el archivo hosts: $hostsPath" }
$lines = @(Get-Content -LiteralPath $hostsPath | Where-Object { $_ -notmatch '(?i)\b(receton\.test|api\.receton\.test)\b' })
$lines += ''
$lines += '# RecetON LAN - apuntar la laptop cliente a la laptop servidor'
$lines += "$ServerIp receton.test api.receton.test"
Set-Content -LiteralPath $hostsPath -Value $lines -Encoding Ascii
ipconfig /flushdns | Out-Null
Write-Host ''
Write-Host 'Dominio de RecetON configurado en esta laptop cliente.' -ForegroundColor Green
Write-Host "Servidor: $ServerIp"
Write-Host 'Abre: http://receton.test:4200'
Write-Host 'Si la IP del servidor cambia, repite el script con la IP nueva.'
