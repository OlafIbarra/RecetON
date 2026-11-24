@echo off
setlocal
echo === Diagnostico de RecetON por dominio estable ===
echo Frontend: http://receton.test:4200
echo API:      http://api.receton.test:8080
echo.
echo [1] Resolucion del dominio:
ping -n 1 receton.test
echo.
echo [2] Backend /saludo (esperado HTTP 200):
curl.exe -i --max-time 5 http://api.receton.test:8080/saludo
echo.
echo [3] Claves publicas OAuth/JWT (esperado HTTP 200):
curl.exe -i --max-time 5 http://api.receton.test:8080/oauth2/jwks
echo.
echo [4] API sin token (esperado HTTP 401):
curl.exe -i --max-time 5 http://api.receton.test:8080/api/categorias
echo.
echo Si el dominio resuelve a una IP antigua, actualiza hosts usando el script del servidor y el del cliente.
pause
endlocal
