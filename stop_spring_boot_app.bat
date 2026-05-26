@echo off
setlocal EnableExtensions

cd /d "%~dp0"

echo.
echo Stopping Construction Company Spring Boot...
echo.

docker info >nul 2>&1
if errorlevel 1 (
    echo Docker is not running, so there is nothing to stop.
    goto done
)

docker compose down
if errorlevel 1 (
    echo.
    echo Could not stop the project with Docker Compose.
    echo Useful diagnostics:
    echo   docker compose ps
    goto fail
)

echo.
echo Project stopped.
echo.

:done
if /i "%~1"=="--no-pause" exit /b 0
pause
exit /b 0

:fail
if /i "%~1"=="--no-pause" exit /b 1
pause
exit /b 1
