@echo off
set JDK=D:\jdk
set M2=%USERPROFILE%\.m2\repository\org\xerial\sqlite-jdbc\3.47.1.0\sqlite-jdbc-3.47.1.0.jar

if not exist "%M2%" (
    echo sqlite-jdbc not found in Maven cache. Run run.bat first.
    pause
    exit /b 1
)

echo Compiling...
"%JDK%\bin\javac" -cp "%M2%" SeedBooks.java
if errorlevel 1 ( pause & exit /b 1 )

echo Inserting 10 books...
"%JDK%\bin\java" -cp ".;%M2%" SeedBooks

del SeedBooks.class 2>nul
pause
