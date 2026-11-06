@echo off
chcp 65001 >nul
set "PATH=%PATH%;C:\tools\apache-maven-3.9.9\bin"
echo ====================================================
echo   Đang khởi chạy ứng dụng Quản lý Thư viện...
echo ====================================================
call mvn exec:java
pause
