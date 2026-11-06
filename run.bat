@echo off
chcp 65001 >nul

:: Tự động bổ sung Maven vào PATH nếu chưa có (dùng %USERPROFILE% để tương thích mọi máy)
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    if defined MAVEN_HOME if exist "%MAVEN_HOME%\bin" set "PATH=%PATH%;%MAVEN_HOME%\bin"
    for /d %%d in ("%USERPROFILE%\apache-maven-*" "C:\Program Files\apache-maven-*") do (
        if exist "%%d\bin" set "PATH=%PATH%;%%d\bin"
    )
)

echo ====================================================
echo   Đang khởi chạy ứng dụng Quản lý Thư viện...
echo ====================================================
call .\mvn.cmd exec:java
pause

