@echo off
echo ======================================================================
echo    PACKAGING STANDALONE EXECUTABLE JAR (JARVIS.jar)
echo ======================================================================

call build.bat

if %ERRORLEVEL% neq 0 (
    echo Build failed. Aborting JAR creation.
    exit /b 1
)

echo Manifest-Version: 1.0 > manifest.txt
echo Main-Class: com.jarvis.Main >> manifest.txt
echo Class-Path: . lib/sqlite-jdbc-3.45.1.0.jar >> manifest.txt
echo. >> manifest.txt

jar cfm JARVIS.jar manifest.txt -C bin com

del manifest.txt

if exist JARVIS.jar (
    echo [SUCCESS] Standalone JARVIS.jar generated successfully!
    echo To run: java -jar JARVIS.jar
) else (
    echo [ERROR] Failed to package JARVIS.jar.
)
