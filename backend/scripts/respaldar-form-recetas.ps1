# Crea un respaldo completo de form_recetas en formato custom de PostgreSQL.
# No modifica la base de datos. El respaldo queda fuera de la carpeta del proyecto.
[CmdletBinding()]
param(
    [string]$Destino = (Join-Path $env:LOCALAPPDATA 'RecetON\Respaldos'),
    [string]$HostName,
    [int]$Port = 0,
    [string]$BaseDatos,
    [string]$Usuario
)

$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot 'RecetonPostgreSql.psm1') -Force
$creoPassword = $false
$archivoTemporal = $null
$listaTemporal = $null

try {
    $config = Get-RecetonDatabaseSettings -HostName $HostName -Port $Port -Database $BaseDatos -Username $Usuario
    $pgDump = Resolve-RecetonPostgresTool -ToolName 'pg_dump'
    $pgRestore = Resolve-RecetonPostgresTool -ToolName 'pg_restore'

    if ([string]::IsNullOrWhiteSpace($Destino)) {
        throw 'No se pudo determinar la carpeta de destino. Indica -Destino explícitamente.'
    }
    New-Item -ItemType Directory -Path $Destino -Force | Out-Null
    $destinoResuelto = (Resolve-Path -LiteralPath $Destino).Path

    $marcaTiempo = Get-Date -Format 'yyyyMMdd_HHmmss'
    $archivoFinal = Join-Path $destinoResuelto ("form_recetas_{0}.backup" -f $marcaTiempo)
    $archivoTemporal = "$archivoFinal.partial"
    $listaFinal = "$archivoFinal.list.txt"
    $listaTemporal = "$listaFinal.partial"
    $hashFinal = "$archivoFinal.sha256"

    $creoPassword = Set-RecetonPgPassword

    Write-Host ''
    Write-Host 'Creando respaldo de PostgreSQL...' -ForegroundColor Cyan
    Write-Host ("Servidor: {0}:{1}" -f $config.Host, $config.Port)
    Write-Host ("Base de datos: {0}" -f $config.Database)
    Write-Host ("Usuario: {0}" -f $config.Username)
    Write-Host ("Destino: {0}" -f $archivoFinal)
    Write-Host ''

    & $pgDump --host $config.Host --port "$($config.Port)" --username $config.Username --format=custom --no-owner --no-acl --no-password --verbose --file $archivoTemporal $config.Database
    if ($LASTEXITCODE -ne 0) {
        throw "pg_dump terminó con código $LASTEXITCODE. No se marcó el archivo como respaldo válido."
    }
    if (-not (Test-Path -LiteralPath $archivoTemporal -PathType Leaf) -or (Get-Item -LiteralPath $archivoTemporal).Length -le 0) {
        throw 'pg_dump no generó un archivo de respaldo válido.'
    }

    # Comprueba que pg_restore pueda leer el catálogo del archivo recién creado.
    & $pgRestore --list $archivoTemporal 1> $listaTemporal
    $codigoLista = $LASTEXITCODE
    if ($codigoLista -ne 0 -or -not (Test-Path -LiteralPath $listaTemporal -PathType Leaf) -or (Get-Item -LiteralPath $listaTemporal).Length -le 0) {
        throw 'El archivo se creó, pero pg_restore no pudo leer su catálogo. Se canceló la publicación del respaldo.'
    }

    Move-Item -LiteralPath $archivoTemporal -Destination $archivoFinal
    Move-Item -LiteralPath $listaTemporal -Destination $listaFinal

    $hash = (Get-FileHash -LiteralPath $archivoFinal -Algorithm SHA256).Hash
    Set-Content -LiteralPath $hashFinal -Value $hash -Encoding ASCII

    Write-Host ''
    Write-Host 'RESPALDO CREADO Y VERIFICADO' -ForegroundColor Green
    Write-Host ("Archivo: {0}" -f $archivoFinal)
    Write-Host ("Catálogo: {0}" -f $listaFinal)
    Write-Host ("SHA-256: {0}" -f $hash)
    Write-Host ''
    Write-Host 'Guarda el .backup junto con su .sha256 y su .list.txt.'
}
catch {
    Write-Error $_
    exit 1
}
finally {
    if ($archivoTemporal -and (Test-Path -LiteralPath $archivoTemporal)) {
        Remove-Item -LiteralPath $archivoTemporal -Force -ErrorAction SilentlyContinue
    }
    if ($listaTemporal -and (Test-Path -LiteralPath $listaTemporal)) {
        Remove-Item -LiteralPath $listaTemporal -Force -ErrorAction SilentlyContinue
    }
    Clear-RecetonPgPassword -CreatedByScript $creoPassword
}
