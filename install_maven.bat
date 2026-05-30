@echo off
echo ======================================
echo   Download Maven
echo ======================================
echo.

set MAVEN_ZIP=%TEMP%\maven.zip
set MAVEN_DIR=D:\apache-maven

echo [1/2] Downloading Maven...

powershell -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%MAVEN_ZIP%' -UseBasicParsing"

if not exist "%MAVEN_ZIP%" (
    echo Download failed. Trying mirror...
    powershell -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri 'https://mirrors.tuna.tsinghua.edu.cn/apache/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%MAVEN_ZIP%' -UseBasicParsing"
)

if not exist "%MAVEN_ZIP%" (
    echo.
    echo Download failed. Please download manually:
    echo   1. Open https://maven.apache.org/download.cgi
    echo   2. Download "Binary zip archive"
    echo   3. Extract to D:\apache-maven
    pause
    exit /b 1
)

echo [2/2] Extracting to %MAVEN_DIR%...
if exist "%MAVEN_DIR%" rmdir /s /q "%MAVEN_DIR%"
powershell -Command "Expand-Archive '%MAVEN_ZIP%' '%MAVEN_DIR%' -Force"
del "%MAVEN_ZIP%"

echo.
echo ======================================
echo   Done!
echo ======================================
echo.
echo Maven at: %MAVEN_DIR%\apache-maven-3.9.6
echo.
echo Now run:
echo   %MAVEN_DIR%\apache-maven-3.9.6\bin\mvn spring-boot:run -f D:\javawork\library_system_web\pom.xml
echo.

pause
