@echo off
setlocal enabledelayedexpansion

:: 1. Kiểm tra nếu có biến môi trường MAVEN_HOME hoặc M2_HOME
if defined MAVEN_HOME (
    if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        "%MAVEN_HOME%\bin\mvn.cmd" %*
        exit /b !errorlevel!
    )
)
if defined M2_HOME (
    if exist "%M2_HOME%\bin\mvn.cmd" (
        "%M2_HOME%\bin\mvn.cmd" %*
        exit /b !errorlevel!
    )
)

:: 2. Kiểm tra nếu Maven đã có trong PATH hệ thống (bỏ qua chính file này để tránh đệ quy)
for /f "delims=" %%i in ('where mvn.cmd 2^>nul') do (
    if /i not "%%~fi"=="%~f0" (
        "%%i" %*
        exit /b !errorlevel!
    )
)
for /f "delims=" %%i in ('where mvn 2^>nul') do (
    if /i not "%%~fi"=="%~f0" (
        "%%i" %*
        exit /b !errorlevel!
    )
)

:: 3. Tự động tìm trong thư mục người dùng hiện tại (%USERPROFILE%) hoặc Program Files (dùng wildcard không phụ thuộc tên user)
for /d %%d in ("%USERPROFILE%\apache-maven-*" "C:\Program Files\apache-maven-*" "C:\ProgramData\chocolatey\lib\maven\*") do (
    if exist "%%d\bin\mvn.cmd" (
        "%%d\bin\mvn.cmd" %*
        exit /b !errorlevel!
    )
)

echo [ERROR] Khong tim thay Apache Maven tren may tinh!
echo Vui long cai dat Maven bang lenh: winget install Apache.Maven --version 3.9.9
exit /b 1

