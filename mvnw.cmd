@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Script (Windows)
@REM ----------------------------------------------------------------------------
@echo off
setlocal

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    call mvn %*
    exit /b %ERRORLEVEL%
)

echo Error: Maven (mvn) not found in PATH.
echo Please install Apache Maven (or run via Docker) to build the project.
exit /b 1
