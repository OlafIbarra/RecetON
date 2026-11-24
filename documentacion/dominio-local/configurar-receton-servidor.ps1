[CmdletBinding()]
param(
    [Parameter(Mandatory = $false)]
    [string]$ServerIp
)
$ErrorActionPreference = 'Stop'
$hostsPath = Join-Path $env:WINDIR 'System32\drivers\etc\hosts'

if ([string]::IsNullOrWhiteSpace($ServerIp)) {
    $configs = @(Get-NetIPConfiguration | Where-Object {
        $_.IPv4DefaultGateway -and $_.IPv4Address
    })
    foreach ($config in $configs) {
        $candidate = $config.IPv4Address | Where-Object {
            $_.IPAddress -and $_.IPAddress -notlike '127.*' -and $_.IPAddress -notlike '169.254.*'
        } | Select-Object -First 1
        if ($candidate) { $ServerIp = $candidate.IPAddress; break }
    }
}

$parsed = $null
if ([string]::IsNullOrWhiteSpace($ServerIp) -or -not [System.Net.IPAddress]::TryParse($ServerIp, [ref]$parsed) -or $parsed.AddressFamily -ne [System.Net.Sockets.AddressFamily]::InterNetwork) {
    throw 'No pude detectar una IPv4 LAN válida. Ejecuta el script con -ServerIp "TU_IP_IPV4" (la del adaptador con puerta de enlace, no la de un adaptador virtual).'
}
if ($ServerIp -like '127.*' -or $ServerIp -like '169.254.*') {
    throw 'La dirección no parece una IPv4 LAN utilizable. Revisa ipconfig.'
}

if (-not (Test-Path -LiteralPath $hostsPath)) { throw "No encontré el archivo hosts: $hostsPath" }
# Elimina las entradas de RecetON antiguas, conservando las demás entradas del archivo.
$lines = @(Get-Content -LiteralPath $hostsPath | Where-Object { $_ -notmatch '(?i)\b(receton\.test|api\.receton\.test)\b' })
$lines += ''
$lines += '# RecetON LAN - actualizado por configurar-receton-servidor.ps1'
$lines += "$ServerIp receton.test api.receton.test"
Set-Content -LiteralPath $hostsPath -Value $lines -Encoding Ascii
ipconfig /flushdns | Out-Null

# Reglas limitadas a equipos de la subred local. Ejecútalo sólo en redes de confianza.
foreach ($port in @(4200, 8080)) {
    $display = "RecetON LAN TCP $port"
    Get-NetFirewallRule -DisplayName $display -ErrorAction SilentlyContinue | Remove-NetFirewallRule -ErrorAction SilentlyContinue
    New-NetFirewallRule -DisplayName $display -Direction Inbound -Action Allow -Protocol TCP -LocalPort $port -RemoteAddress LocalSubnet -Profile Any | Out-Null
}
Write-Host ''
Write-Host 'RecetON se configuró con una dirección estable.' -ForegroundColor Green
Write-Host "IP LAN detectada/configurada: $ServerIp"
Write-Host 'Frontend: http://receton.test:4200'
Write-Host 'API:      http://api.receton.test:8080'
Write-Host ''
Write-Host 'En la laptop cliente, ejecuta configurar-receton-cliente.ps1 -ServerIp con esta misma IP.'
Write-Host 'Si cambias de red, vuelve a ejecutar este script como Administrador.'
