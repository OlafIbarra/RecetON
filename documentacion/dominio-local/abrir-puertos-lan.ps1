# Ejecutar solo en la laptop servidor, desde PowerShell como Administrador.
$ErrorActionPreference = 'Stop'
$identity = [Security.Principal.WindowsIdentity]::GetCurrent()
$principal = [Security.Principal.WindowsPrincipal]::new($identity)
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) { throw 'Abre PowerShell como Administrador y vuelve a ejecutar este script.' }
# Limita las conexiones entrantes al segmento local. No abre PostgreSQL (5432).
$rules = @(
    @{ Name = 'RecetON Angular LAN 4200'; Port = 4200 },
    @{ Name = 'RecetON API LAN 8080'; Port = 8080 }
)
foreach ($rule in $rules) {
    Get-NetFirewallRule -DisplayName $rule.Name -ErrorAction SilentlyContinue | Remove-NetFirewallRule
    New-NetFirewallRule -DisplayName $rule.Name -Direction Inbound -Action Allow -Protocol TCP -LocalPort $rule.Port -Profile Private -RemoteAddress LocalSubnet | Out-Null
}
Write-Host 'Reglas creadas para los puertos 4200 y 8080 desde la subred local.' -ForegroundColor Green
Write-Host 'Comprueba que la red Wi-Fi de confianza tenga perfil Privado en Windows.'
