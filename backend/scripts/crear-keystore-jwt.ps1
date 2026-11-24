# Genera una pareja RSA persistente en un almacén PKCS#12 fuera del código fuente.
# Ejecutar desde cualquier ubicación. No sobrescribe un almacén existente.
$ErrorActionPreference = 'Stop'

$raizBackend = Split-Path -Parent $PSScriptRoot
$directorioSecretos = Join-Path $raizBackend 'secrets'
$archivoKeystore = Join-Path $directorioSecretos 'receton-jwt.p12'
$alias = 'receton-jwt'
$rutaKeytool = $null
$seGeneroArchivo = $false

try {
    $keytool = Get-Command 'keytool.exe' -ErrorAction SilentlyContinue
    if (-not $keytool) { $keytool = Get-Command 'keytool' -ErrorAction SilentlyContinue }
    if ($keytool) {
        $rutaKeytool = $keytool.Source
    } elseif ($env:JAVA_HOME) {
        $candidato = Join-Path $env:JAVA_HOME 'bin\keytool.exe'
        if (Test-Path $candidato) { $rutaKeytool = $candidato }
    }
    if (-not $rutaKeytool) {
        throw 'No se encontró keytool. Instala/usa un JDK 17 y asegúrate de que JAVA_HOME o PATH apunten a él.'
    }

    if (Test-Path $archivoKeystore) {
        throw "Ya existe $archivoKeystore. No se sobrescribió para evitar perder la llave JWT activa."
    }

    New-Item -ItemType Directory -Path $directorioSecretos -Force | Out-Null

    $passwordSegura = Read-Host 'Crea una contraseña para el almacén JWT (mínimo 16 caracteres; no se mostrará)' -AsSecureString
    if ($passwordSegura.Length -lt 16) {
        throw 'La contraseña del almacén JWT debe tener al menos 16 caracteres.'
    }
    $confirmacionSegura = Read-Host 'Confirma la contraseña del almacén JWT' -AsSecureString

    $bstr1 = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($passwordSegura)
    $bstr2 = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($confirmacionSegura)
    try {
        $passwordTexto = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr1)
        $confirmacionTexto = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr2)
        if ($passwordTexto -cne $confirmacionTexto) {
            throw 'Las contraseñas no coinciden. No se creó el almacén.'
        }
        $env:RECETON_JWT_KEYSTORE_PASSWORD = $passwordTexto
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr1)
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr2)
        $passwordSegura.Dispose()
        $confirmacionSegura.Dispose()
    }

    Write-Host ''
    Write-Host 'Generando llave RSA de 3072 bits en PKCS#12...' -ForegroundColor Green
    & $rutaKeytool -genkeypair `
        -alias $alias `
        -keyalg RSA `
        -keysize 3072 `
        -sigalg SHA256withRSA `
        -validity 3650 `
        -dname 'CN=RecetON JWT Signing' `
        -storetype PKCS12 `
        -keystore $archivoKeystore `
        -storepass:env RECETON_JWT_KEYSTORE_PASSWORD `
        -keypass:env RECETON_JWT_KEYSTORE_PASSWORD `
        -noprompt

    if ($LASTEXITCODE -ne 0 -or -not (Test-Path $archivoKeystore)) {
        throw 'keytool no pudo generar el almacén PKCS#12.'
    }
    $seGeneroArchivo = $true

    Write-Host ''
    Write-Host 'Almacén JWT creado correctamente:' -ForegroundColor Green
    Write-Host $archivoKeystore
    Write-Host 'Alias: receton-jwt'
    Write-Host 'La contraseña no se guardó en el proyecto.'
    Write-Host 'No compartas ni subas secrets/receton-jwt.p12.'
}
catch {
    Write-Error $_
    if ((Test-Path $archivoKeystore) -and -not $seGeneroArchivo) {
        # Elimina solo un archivo parcial de esta ejecución, nunca un almacén previo.
        Remove-Item $archivoKeystore -Force -ErrorAction SilentlyContinue
    }
    exit 1
}
finally {
    Remove-Item Env:\RECETON_JWT_KEYSTORE_PASSWORD -ErrorAction SilentlyContinue
    $passwordTexto = $null
    $confirmacionTexto = $null
}
