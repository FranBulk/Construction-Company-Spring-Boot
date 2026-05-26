@echo off
setlocal EnableExtensions

set "APP_URL=http://127.0.0.1:8080/"
set "HEALTH_URL=http://127.0.0.1:8080/api/health"
set "DOCKER_DESKTOP=C:\Program Files\Docker\Docker\Docker Desktop.exe"

cd /d "%~dp0"

echo.
echo Starting Construction Company Spring Boot...
echo.

docker info >nul 2>&1
if errorlevel 1 (
    echo Docker is not ready. Trying to start Docker Desktop...
    if exist "%DOCKER_DESKTOP%" (
        start "" "%DOCKER_DESKTOP%"
    ) else (
        echo Docker Desktop was not found at:
        echo %DOCKER_DESKTOP%
        goto fail
    )

    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "$ready = $false; " ^
        "for ($i = 0; $i -lt 60; $i++) { " ^
        "  docker info *> $null; " ^
        "  if ($LASTEXITCODE -eq 0) { $ready = $true; break } " ^
        "  Start-Sleep -Seconds 2 " ^
        "}; " ^
        "if (-not $ready) { exit 1 }"

    if errorlevel 1 (
        echo Docker did not become ready in time.
        goto fail
    )
)

echo Building and starting containers...
docker compose up -d --build
if errorlevel 1 (
    echo Docker Compose failed to start the project.
    goto fail
)

echo.
echo Waiting for Spring Boot, RabbitMQ, and Oracle connection...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$url = '%HEALTH_URL%'; " ^
    "$ready = $false; " ^
    "for ($i = 0; $i -lt 90; $i++) { " ^
    "  try { " ^
    "    $response = Invoke-RestMethod -Uri $url -TimeoutSec 3; " ^
    "    if ($response.status -eq 'UP' -and $response.database -eq 'UP') { $ready = $true; break } " ^
    "  } catch {} " ^
    "  Start-Sleep -Seconds 2 " ^
    "}; " ^
    "if (-not $ready) { exit 1 }"

if errorlevel 1 (
    echo The application did not become healthy in time.
    echo.
    echo Useful diagnostics:
    echo   docker compose ps
    echo   docker compose logs app
    goto fail
)

echo.
echo Project is ready.
echo Frontend:
echo   %APP_URL%
echo.
echo RabbitMQ Management:
echo   http://127.0.0.1:15672/
echo   User: guest
echo   Password: guest
echo.
goto done

:fail
echo.
echo Startup failed. Check Docker Desktop and make sure Oracle is running locally.
echo.
exit /b 1

:done
if /i "%~1"=="--no-pause" exit /b 0
pause
