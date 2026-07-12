@echo off
chcp 65001 >nul
title 视频会议项目 ECS 一键部署

echo ============================================================
echo   视频会议项目 ECS 一键部署脚本
echo   公网IP: 59.110.171.175
echo ============================================================
echo.
echo 正在下载并启动部署脚本...
echo.

powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://gitee.com/ljl-one/video-app/raw/main/deploy-ecs.ps1' -OutFile '%TEMP%\deploy-ecs.ps1' -TimeoutSec 120; powershell -ExecutionPolicy Bypass -File '%TEMP%\deploy-ecs.ps1'"

pause
