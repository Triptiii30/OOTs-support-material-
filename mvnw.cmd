@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Script
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "LOCAL_MAVEN=C:\Users\Lenovo\.gemini\antigravity\scratch\tools\apache-maven-3.9.9\bin\mvn.cmd"
if exist "%LOCAL_MAVEN%" (
    call "%LOCAL_MAVEN%" %*
    exit /b %ERRORLEVEL%
)

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    call mvn %*
    exit /b %ERRORLEVEL%
)

echo Error: Maven not found. Please install Maven or add it to PATH.
exit /b 1
