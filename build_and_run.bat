@echo off
call build.bat
if %ERRORLEVEL% equ 0 (
    echo Starting JARVIS GUI...
    call run.bat
)
