param(
    [Parameter(Mandatory = $true)]
    [string]$ServerIp
)
$ErrorActionPreference = 'Stop'

# Validate IPv4 using a strict pattern. This avoids a PowerShell/.NET TryParse
# overload issue seen on some Windows PowerShell installations.
$ipv4Pattern = '^(?:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)\.){3}(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)$'
if ($ServerIp -notmatch $ipv4Pattern -or $ServerIp -eq '127.0.0.1') {
    throw "Indica una IPv4 válida de la laptop servidor; recibí '$ServerIp'. No uses 127.0.0.1."
}

$hostsPath = Join-Path $env:WINDIR 'System32\drivers\etc\hosts'
$backupPath = "$hostsPath.receton-lan.bak"
$identity = [Security.Principal.WindowsIdentity]::GetCurrent()
$principal = [Security.Principal.WindowsPrincipal]::new($identity)
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    throw 'Abre PowerShell como Administrador y vuelve a ejecutar este script.'
}
if (-not (Test-Path -LiteralPath $hostsPath)) {
    throw "No se encontró el archivo hosts en: $hostsPath"
}
if (-not (Test-Path -LiteralPath $backupPath)) {
    Copy-Item -LiteralPath $hostsPath -Destination $backupPath
}

$names = @('receton.test', 'api.receton.test')
$content = Get-Content -LiteralPath $hostsPath
$filtered = foreach ($line in $content) {
    $trimmed = $line.Trim()
    if ($trimmed.StartsWith('#') -or $trimmed.Length -eq 0) {
        $line
        continue
    }
    $parts = $trimmed -split '\s+'
    $activeNames = @($parts | Select-Object -Skip 1)
    if (-not ($activeNames | Where-Object { $names -contains $_ })) { $line }
}

$filtered | Set-Content -LiteralPath $hostsPath -Encoding ascii
Add-Content -LiteralPath $hostsPath -Value "$ServerIp receton.test" -Encoding ascii
Add-Content -LiteralPath $hostsPath -Value "$ServerIp api.receton.test" -Encoding ascii
ipconfig /flushdns | Out-Null
Write-Host "RecetON LAN configurado hacia $ServerIp" -ForegroundColor Green
Write-Host 'Web: http://receton.test:4200'
Write-Host 'API: http://api.receton.test:8080'
Write-Host 'Ejecuta también este script en cada computadora cliente con la misma IPv4 del servidor.'
