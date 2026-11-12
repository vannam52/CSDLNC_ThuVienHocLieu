@echo off
chcp 65001 >nul
title Dashboard Thu Vien Hoc Lieu - CSDL NC De Tai 3
cd /d "%~dp0"

:: Tu dong bo sung Maven vao PATH neu chua co
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    if defined MAVEN_HOME if exist "%MAVEN_HOME%\bin" set "PATH=%PATH%;%MAVEN_HOME%\bin"
    for /d %%d in ("%USERPROFILE%\apache-maven-*" "C:\Program Files\apache-maven-*") do (
        if exist "%%d\bin" set "PATH=%PATH%;%%d\bin"
    )
)

echo.
echo =======================================================
echo   Dashboard Thu Vien Hoc Lieu - CSDL Nang cao De tai 3
echo =======================================================
echo.
echo  Dang bien dich va khoi dong giao dien GUI Dashboard...
echo.
call .\mvn.cmd compile exec:java -Pgui -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo [LOI] Khong the khoi chay GUI. Vui long xem thong bao loi o tren.
    pause
    exit /b 1
)
pause
