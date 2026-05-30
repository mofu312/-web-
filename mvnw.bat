@echo off
setlocal

set MVNW_DIR=%~dp0.mvn\wrapper
set MVNW_JAR=%MVNW_DIR%\maven-wrapper.jar
set MVNW_PROPS=%MVNW_DIR%\maven-wrapper.properties

if not exist "%MVNW_JAR%" (
    echo Downloading Maven Wrapper...
    if not exist "%MVNW_DIR%" mkdir "%MVNW_DIR%"

    powershell -Command ^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
         Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip' ^
         -OutFile '%TEMP%\maven.zip' -UseBasicParsing"

    powershell -Command "Expand-Archive '%TEMP%\maven.zip' '%TEMP%\maven-tmp' -Force"
    copy /y "%TEMP%\maven-tmp\apache-maven-3.9.9\lib\*" "%MVNW_DIR%\"
    rmdir /s /q "%TEMP%\maven-tmp" 2>nul
    del "%TEMP%\maven.zip" 2>nul
)

echo Downloading Maven Wrapper JAR...
powershell -Command ^
    "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
     Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar' ^
     -OutFile '%MVNW_JAR%' -UseBasicParsing"

(echo distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip
echo wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
) > "%MVNW_PROPS%"

set JAVA_HOME=D:\jdk
"%JAVA_HOME%\bin\java" -jar "%MVNW_JAR%" %*
exit /b %errorlevel%
