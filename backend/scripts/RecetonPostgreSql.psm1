# Utilidades compartidas para los scripts de respaldo y recuperación de RecetON.
# Compatible con Windows PowerShell 5.1.

function Resolve-RecetonPostgresTool {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [ValidateSet('pg_dump', 'pg_restore')]
        [string]$ToolName
    )

    $fileName = "$ToolName.exe"

    if (-not [string]::IsNullOrWhiteSpace($env:PG_BIN)) {
        if (Test-Path -LiteralPath $env:PG_BIN -PathType Leaf) {
            $candidate = (Resolve-Path -LiteralPath $env:PG_BIN).Path
            if ([IO.Path]::GetFileName($candidate) -ieq $fileName) {
                return $candidate
            }
        }
        elseif (Test-Path -LiteralPath $env:PG_BIN -PathType Container) {
            $candidate = Join-Path $env:PG_BIN $fileName
            if (Test-Path -LiteralPath $candidate -PathType Leaf) {
                return (Resolve-Path -LiteralPath $candidate).Path
            }
        }
        throw "PG_BIN está definido, pero no contiene $fileName. Ajusta PG_BIN a la carpeta bin de PostgreSQL o elimina esa variable."
    }

    $command = Get-Command $fileName -ErrorAction SilentlyContinue
    if (-not $command) {
        $command = Get-Command $ToolName -ErrorAction SilentlyContinue
    }
    if ($command) {
        return $command.Source
    }

    $programRoots = @($env:ProgramW6432, $env:ProgramFiles, ${env:ProgramFiles(x86)}) |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
        Select-Object -Unique

    foreach ($programRoot in $programRoots) {
        $postgresRoot = Join-Path $programRoot 'PostgreSQL'
        if (Test-Path -LiteralPath $postgresRoot -PathType Container) {
            $installations = Get-ChildItem -LiteralPath $postgresRoot -Directory -ErrorAction SilentlyContinue |
                Sort-Object Name -Descending
            foreach ($installation in $installations) {
                $candidate = Join-Path (Join-Path $installation.FullName 'bin') $fileName
                if (Test-Path -LiteralPath $candidate -PathType Leaf) {
                    return $candidate
                }
            }
        }
    }

    throw "No se encontró $fileName. Agrega PostgreSQL\bin al PATH o define PG_BIN con la ruta de esa carpeta."
}

function Get-RecetonDatabaseSettings {
    [CmdletBinding()]
    param(
        [string]$HostName,
        [int]$Port = 0,
        [string]$Database,
        [string]$Username
    )

    $urlHost = $null
    $urlPort = 0
    $urlDatabase = $null

    if (-not [string]::IsNullOrWhiteSpace($env:DB_URL)) {
        $match = [regex]::Match(
            $env:DB_URL,
            '^jdbc:postgresql://(?<host>[^/:?]+)(:(?<port>\d+))?/(?<database>[^?;]+)'
        )
        if (-not $match.Success) {
            throw 'DB_URL no tiene el formato esperado jdbc:postgresql://host:puerto/base_de_datos.'
        }
        $urlHost = $match.Groups['host'].Value
        if ($match.Groups['port'].Success) {
            $urlPort = [int]$match.Groups['port'].Value
        }
        $urlDatabase = [Uri]::UnescapeDataString($match.Groups['database'].Value)
    }

    if ([string]::IsNullOrWhiteSpace($HostName)) {
        if (-not [string]::IsNullOrWhiteSpace($env:PGHOST)) {
            $HostName = $env:PGHOST
        }
        elseif ($urlHost) {
            $HostName = $urlHost
        }
        else {
            $HostName = 'localhost'
        }
    }

    if ($Port -le 0) {
        if (-not [string]::IsNullOrWhiteSpace($env:PGPORT)) {
            $Port = [int]$env:PGPORT
        }
        elseif ($urlPort -gt 0) {
            $Port = $urlPort
        }
        else {
            $Port = 5432
        }
    }

    if ([string]::IsNullOrWhiteSpace($Database)) {
        if (-not [string]::IsNullOrWhiteSpace($env:PGDATABASE)) {
            $Database = $env:PGDATABASE
        }
        elseif ($urlDatabase) {
            $Database = $urlDatabase
        }
        else {
            $Database = 'form_recetas'
        }
    }

    if ([string]::IsNullOrWhiteSpace($Username)) {
        if (-not [string]::IsNullOrWhiteSpace($env:DB_USERNAME)) {
            $Username = $env:DB_USERNAME
        }
        elseif (-not [string]::IsNullOrWhiteSpace($env:PGUSER)) {
            $Username = $env:PGUSER
        }
        else {
            $Username = 'postgres'
        }
    }

    return [PSCustomObject]@{
        Host = $HostName
        Port = $Port
        Database = $Database
        Username = $Username
    }
}

function Set-RecetonPgPassword {
    [CmdletBinding()]
    param()

    if (-not [string]::IsNullOrWhiteSpace($env:PGPASSWORD)) {
        return $false
    }

    if (-not [string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
        $env:PGPASSWORD = $env:DB_PASSWORD
        return $true
    }

    $securePassword = Read-Host 'Contraseña del usuario PostgreSQL (no se mostrará)' -AsSecureString
    if ($securePassword.Length -eq 0) {
        $securePassword.Dispose()
        throw 'La contraseña de PostgreSQL no puede estar vacía.'
    }

    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
        $securePassword.Dispose()
    }

    return $true
}

function Clear-RecetonPgPassword {
    [CmdletBinding()]
    param([bool]$CreatedByScript)

    if ($CreatedByScript) {
        Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
    }
}

Export-ModuleMember -Function Resolve-RecetonPostgresTool, Get-RecetonDatabaseSettings, Set-RecetonPgPassword, Clear-RecetonPgPassword
