# Inicia Spring Boot sin guardar las contraseñas en archivos del proyecto.
# Ejecutar desde cualquier ubicación. Requiere secrets/receton-jwt.p12 previamente creado.
$ErrorActionPreference = 'Stop'

$raizBackend = Split-Path -Parent $PSScriptRoot
Set-Location $raizBackend

$creoDbPassword = $false
$creoJwtPassword = $false
$exitCode = 0

function Convertir-SecureStringATexto([System.Security.SecureString] $valorSeguro) {
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($valorSeguro)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}

try {
    if (-not (Test-Path '.\gradlew.bat')) {
        throw 'No se encontró gradlew.bat en la carpeta backend.'
    }

    $rutaKeystore = if ([string]::IsNullOrWhiteSpace($env:RECETON_JWT_KEYSTORE_PATH)) { 'secrets/receton-jwt.p12' } else { $env:RECETON_JWT_KEYSTORE_PATH }
    if (-not (Test-Path $rutaKeystore -PathType Leaf)) {
        throw "No se encontró el almacén JWT en '$rutaKeystore'. Ejecuta primero .\scripts\crear-keystore-jwt.ps1."
    }
    if ([string]::IsNullOrWhiteSpace($env:RECETON_JWT_KEY_ALIAS)) {
        $env:RECETON_JWT_KEY_ALIAS = 'receton-jwt'
    }

    if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
        $dbSeguro = Read-Host 'Escribe la contraseña actual de PostgreSQL (no se mostrará)' -AsSecureString
        if ($dbSeguro.Length -eq 0) { throw 'La contraseña de PostgreSQL no puede estar vacía.' }
        $env:DB_PASSWORD = Convertir-SecureStringATexto $dbSeguro
        $dbSeguro.Dispose()
        $creoDbPassword = $true
    }

    if ([string]::IsNullOrWhiteSpace($env:RECETON_JWT_KEYSTORE_PASSWORD)) {
        $jwtSeguro = Read-Host 'Escribe la contraseña del almacén JWT PKCS#12 (no se mostrará)' -AsSecureString
        if ($jwtSeguro.Length -lt 16) { throw 'La contraseña del almacén JWT debe tener al menos 16 caracteres.' }
        $env:RECETON_JWT_KEYSTORE_PASSWORD = Convertir-SecureStringATexto $jwtSeguro
        $jwtSeguro.Dispose()
        $creoJwtPassword = $true
    }

    if ([string]::IsNullOrWhiteSpace($env:RECETON_SESSION_COOKIE_SECURE)) {
        $urlFrontend = if ([string]::IsNullOrWhiteSpace($env:RECETON_FRONTEND_URL)) {
            'http://receton.test:4200'
        } else {
            $env:RECETON_FRONTEND_URL
        }
        try {
            $uriFrontend = [Uri]$urlFrontend
            if (-not $uriFrontend.IsAbsoluteUri -or $uriFrontend.Scheme -notin @('http', 'https')) {
                throw 'Esquema de URL no permitido.'
            }
            $env:RECETON_SESSION_COOKIE_SECURE = if ($uriFrontend.Scheme -eq 'https') { 'true' } else { 'false' }
        } catch {
            throw 'RECETON_FRONTEND_URL no es una URL válida. Configura una URL http:// o https:// antes de iniciar.'
        }
    }

    if ([string]::IsNullOrWhiteSpace($env:DB_URL)) {
        $env:DB_URL = 'jdbc:postgresql://localhost:5432/form_recetas'
    }
    if ([string]::IsNullOrWhiteSpace($env:DB_USERNAME)) {
        $env:DB_USERNAME = 'postgres'
    }

    Write-Host ''
    Write-Host 'Iniciando RecetON Backend...' -ForegroundColor Green
    Write-Host ('URL de base de datos: ' + $env:DB_URL)
    Write-Host ('Usuario de base de datos: ' + $env:DB_USERNAME)
    Write-Host ('Almacén JWT: ' + $rutaKeystore)
    Write-Host ('Alias JWT: ' + $env:RECETON_JWT_KEY_ALIAS)
    Write-Host ('Cookie de sesión Secure: ' + $env:RECETON_SESSION_COOKIE_SECURE)
    Write-Host 'Las contraseñas no se mostrarán.'
    Write-Host 'Dirección de escucha predeterminada: 127.0.0.1'
    Write-Host ''

    & .\gradlew.bat bootRun
    $exitCode = $LASTEXITCODE
}
catch {
    Write-Error $_
    $exitCode = 1
}
finally {
    if ($creoDbPassword) { Remove-Item Env:\DB_PASSWORD -ErrorAction SilentlyContinue }
    if ($creoJwtPassword) { Remove-Item Env:\RECETON_JWT_KEYSTORE_PASSWORD -ErrorAction SilentlyContinue }
}

$global:LASTEXITCODE = $exitCode
if ($exitCode -eq 0) {
    Write-Host 'Spring Boot terminó correctamente.' -ForegroundColor Green
} else {
    Write-Warning "Spring Boot terminó con código de salida $exitCode. Revisa los mensajes anteriores."
}
return
