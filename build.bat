@echo off
echo ======================================================================
echo    COMPILING JARVIS: KTU S3 CSE OOP MINI PROJECT (CSL 203)
echo    Engineering Team: Jobin, Farsin, Sharon, Theerth
echo ======================================================================

if not exist bin mkdir bin

set "JARVIS_CLASSPATH=src"
if exist lib set "JARVIS_CLASSPATH=%JARVIS_CLASSPATH%;lib/*"

echo Compiling Java source files to bin/...
javac -d bin -encoding UTF-8 -cp "%JARVIS_CLASSPATH%" src\com\jarvis\Main.java src\com\jarvis\core\*.java src\com\jarvis\audio\*.java src\com\jarvis\speech\*.java src\com\jarvis\commands\*.java src\com\jarvis\database\*.java src\com\jarvis\scheduler\*.java src\com\jarvis\gui\*.java src\com\jarvis\TestRunner.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation completed with 0 errors!
) else (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)
