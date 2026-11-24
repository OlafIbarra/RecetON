@echo off
setlocal
set "BASE=http://api.receton.test:8080"
echo === Diagnostico de conectividad RecetON ===
echo Servidor configurado: %BASE%
echo.
echo [1/3] Endpoint publico /saludo
curl.exe -i "%BASE%/saludo"
echo.
echo [2/3] Claves publicas JWT /oauth2/jwks
curl.exe -i "%BASE%/oauth2/jwks"
echo.
echo [3/3] API protegida sin token; se espera 401
curl.exe -i "%BASE%/api/categorias"
echo.
echo Si la IP cambia, edita BASE en este archivo.
echo El 401 de la tercera prueba es esperado sin un JWT.
endlocal
