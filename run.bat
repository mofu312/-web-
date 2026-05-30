@echo off
title Library System Web
set MVN=D:\apache-maven-3.9.16\bin\mvn
set PROJECT=D:\javawork\library_system_web

echo ======================================
echo   Library System Web v1.0
echo ======================================
echo.

cd /d "%PROJECT%"

echo [1/2] Downloading dependencies...
call "%MVN%" -q dependency:resolve
if errorlevel 1 (
    echo [FAIL] Check pom.xml
    pause
    exit /b 1
)

echo [2/2] Starting server at http://localhost:8080
echo.
call "%MVN%" spring-boot:run
pause
