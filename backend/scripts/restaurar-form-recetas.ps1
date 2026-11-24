# Restaura un respaldo en una base de datos destino EXISTENTE.
# Por seguridad, no elimina la base de datos. La limpieza de objetos requiere -LimpiarDestino y confirmación exacta.
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$Archivo,

    [Parameter(Mandatory = $true)]
    [string]$BaseDatosDestino,

    [string]$HostName = 'localhost',
    [int]$Port = 5432,
    [string]$Usuario = 'postgres',

    [switch]$LimpiarDestino
)

$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot 'RecetonPostgreSql.psm1') -Force
$creoPassword = $false
$listaTemporal = Join-Path $env:TEMP ("receton_restore_{0}.txt" -f [Guid]::NewGuid().ToString('N'))

try {
    if (-not (Test-Path -LiteralPath $Archivo -PathType Leaf)) {
        throw "No se encontró el archivo de respaldo: $Archivo"
    }
    if ([string]::IsNullOrWhiteSpace($BaseDatosDestino)) {
        throw 'Indica una base de datos destino existente.'
    }

    $archivoResuelto = (Resolve-Path -LiteralPath $Archivo).Path
    $pgRestore = Resolve-RecetonPostgresTool -ToolName 'pg_restore'

    # Validar el respaldo antes de solicitar credenciales o tocar la base destino.
    & $pgRestore --list $archivoResuelto 1> $listaTemporal
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $listaTemporal) -or (Get-Item -LiteralPath $listaTemporal).Length -le 0) {
        throw 'El respaldo no pudo validarse con pg_restore. No se realizó ninguna restauración.'
    }

    if ($LimpiarDestino) {
        $textoConfirmacion = "LIMPIAR $BaseDatosDestino"
        Write-Warning "Se reemplazarán los objetos existentes dentro de la base '$BaseDatosDestino'."
        Write-Warning 'La base de datos no será eliminada, pero sus objetos pueden borrarse y recrearse.'
    }
    else {
        $textoConfirmacion = "RESTAURAR $BaseDatosDestino"
        Write-Warning "Se restaurará en '$BaseDatosDestino' sin limpiar objetos existentes. Usa preferentemente una base vacía."
    }

    $confirmacion = Read-Host "Para continuar, escribe exactamente: $textoConfirmacion"
    if ($confirmacion -cne $textoConfirmacion) {
        throw 'Confirmación incorrecta. Restauración cancelada; la base de datos no fue modificada.'
    }

    $creoPassword = Set-RecetonPgPassword

    $argumentos = @(
        '--host', $HostName,
        '--port', "$Port",
        '--username', $Usuario,
        '--dbname', $BaseDatosDestino,
        '--no-owner',
        '--no-acl',
        '--exit-on-error',
        '--single-transaction',
        '--verbose',
        '--no-password'
    )
    if ($LimpiarDestino) {
        $argumentos += @('--clean', '--if-exists')
    }
    $argumentos += $archivoResuelto

    Write-Host ''
    Write-Host 'Iniciando restauración...' -ForegroundColor Cyan
    Write-Host ("Servidor: {0}:{1}" -f $HostName, $Port)
    Write-Host ("Base destino: {0}" -f $BaseDatosDestino)
    Write-Host ("Usuario: {0}" -f $Usuario)

    & $pgRestore @argumentos
    if ($LASTEXITCODE -ne 0) {
        throw "pg_restore terminó con código $LASTEXITCODE. Revisa el mensaje de PostgreSQL. La transacción se solicitó como atómica para evitar una restauración parcial."
    }

    Write-Host ''
    Write-Host 'RESTAURACIÓN COMPLETADA' -ForegroundColor Green
    Write-Host ("Base de datos destino: {0}" -f $BaseDatosDestino)
    Write-Host 'Recuerda comprobar la conexión de la aplicación y los permisos del usuario PostgreSQL.'
}
catch {
    Write-Error $_
    exit 1
}
finally {
    if (Test-Path -LiteralPath $listaTemporal) {
        Remove-Item -LiteralPath $listaTemporal -Force -ErrorAction SilentlyContinue
    }
    Clear-RecetonPgPassword -CreatedByScript $creoPassword
}
