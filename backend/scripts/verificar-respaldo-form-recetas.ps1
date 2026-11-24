# Comprueba que pg_restore pueda leer el catálogo del respaldo y valida su SHA-256, si existe.
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$Archivo
)

$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot 'RecetonPostgreSql.psm1') -Force
$listaTemporal = Join-Path $env:TEMP ("receton_pgrestore_{0}.txt" -f [Guid]::NewGuid().ToString('N'))

try {
    if (-not (Test-Path -LiteralPath $Archivo -PathType Leaf)) {
        throw "No se encontró el respaldo: $Archivo"
    }
    $archivoResuelto = (Resolve-Path -LiteralPath $Archivo).Path
    if ((Get-Item -LiteralPath $archivoResuelto).Length -le 0) {
        throw 'El archivo de respaldo está vacío.'
    }

    $pgRestore = Resolve-RecetonPostgresTool -ToolName 'pg_restore'
    & $pgRestore --list $archivoResuelto 1> $listaTemporal
    if ($LASTEXITCODE -ne 0) {
        throw "pg_restore no pudo leer el respaldo (código $LASTEXITCODE)."
    }
    if (-not (Test-Path -LiteralPath $listaTemporal) -or (Get-Item -LiteralPath $listaTemporal).Length -le 0) {
        throw 'pg_restore no devolvió un catálogo legible.'
    }

    $hashActual = (Get-FileHash -LiteralPath $archivoResuelto -Algorithm SHA256).Hash
    $archivoHash = "$archivoResuelto.sha256"
    if (Test-Path -LiteralPath $archivoHash -PathType Leaf) {
        $hashRegistrado = (Get-Content -LiteralPath $archivoHash -Raw).Trim()
        if ($hashRegistrado -cne $hashActual) {
            throw 'El SHA-256 no coincide. El archivo pudo modificarse o dañarse.'
        }
        Write-Host 'SHA-256: coincide.' -ForegroundColor Green
    }
    else {
        Write-Warning 'No se encontró el archivo .sha256 junto al respaldo; se verificó el catálogo, pero no hay hash de referencia para comparar.'
    }

    $lineasCatalogo = @(Get-Content -LiteralPath $listaTemporal)
    Write-Host ''
    Write-Host 'RESPALDO LEGIBLE POR PG_RESTORE' -ForegroundColor Green
    Write-Host ("Archivo: {0}" -f $archivoResuelto)
    Write-Host ("Tamaño: {0:N2} MB" -f ((Get-Item -LiteralPath $archivoResuelto).Length / 1MB))
    Write-Host ("SHA-256: {0}" -f $hashActual)
    Write-Host ("Líneas de catálogo: {0}" -f $lineasCatalogo.Count)
    Write-Host ''
    Write-Host 'Esta comprobación valida la lectura del archivo y su hash, no sustituye una restauración de prueba.'
}
catch {
    Write-Error $_
    exit 1
}
finally {
    if (Test-Path -LiteralPath $listaTemporal) {
        Remove-Item -LiteralPath $listaTemporal -Force -ErrorAction SilentlyContinue
    }
}
