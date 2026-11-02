@echo off
echo ======================================================================
echo    LAUNCHING JARVIS: Intelligent Desktop Voice Assistant
echo    KTU S3 CSE OOP Mini Project • CSL 203
echo ======================================================================

if not exist bin\com\jarvis\Main.class (
    echo Bin directory is missing compiled classes. Running build first...
    call build.bat
    if errorlevel 1 exit /b 1
)

java -cp "bin;lib/*" com.jarvis.Main
