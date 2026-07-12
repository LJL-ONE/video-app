# ============================================================
# 视频会议项目 ECS 一键部署脚本（无需键盘输入）
# 使用方法：右键此文件 → 使用 PowerShell 运行
# ============================================================

$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

function Write-Step($msg) { Write-Host "`n>>> $msg" -ForegroundColor Green }
function Write-OK($msg) { Write-Host "    [OK] $msg" -ForegroundColor Cyan }
function Write-Warn($msg) { Write-Host "    [!] $msg" -ForegroundColor Yellow }
function Write-Err($msg) { Write-Host "    [X] $msg" -ForegroundColor Red }

# ===== 检查是否以管理员运行 =====
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Err "请以管理员身份运行此脚本！右键 → 使用 PowerShell 运行"
    Read-Host "按回车退出"
    exit 1
}

Write-Host "`n================================================" -ForegroundColor Green
Write-Host "  视频会议项目 ECS 一键部署脚本" -ForegroundColor Green
Write-Host "  公网IP: 59.110.171.175" -ForegroundColor Green
Write-Host "================================================`n" -ForegroundColor Green

# ===== 第1步：检查 WSL 是否已安装 =====
Write-Step "第1步：检查 WSL2 是否已安装"
$wslOk = $false
try {
    $wslOutput = wsl --status 2>&1
    if ($LASTEXITCODE -eq 0) {
        $wslOk = $true
        Write-OK "WSL 已安装"
    }
} catch { }

if (-not $wslOk) {
    Write-Warn "WSL 未安装，正在安装..."
    wsl --install --no-distribution
    Write-Warn "WSL 安装完成，需要重启服务器！"
    Write-Warn "重启后再次运行此脚本即可继续。"
    Write-Host "`n请按任意键重启服务器..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    Restart-Computer -Force
    exit 0
}

# ===== 第2步：启动 Docker Desktop =====
Write-Step "第2步：启动 Docker Desktop"
$dockerOk = $false
for ($i = 0; $i -lt 12; $i++) {
    try {
        $result = docker info 2>&1
        if ($result -match "Server Version") {
            $dockerOk = $true
            Write-OK "Docker 已就绪"
            break
        }
    } catch { }

    if ($i -eq 0) {
        Write-Warn "正在启动 Docker Desktop..."
        Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe" -ErrorAction SilentlyContinue
    }
    Write-Warn "等待 Docker 启动... (第 $($i+1) 次尝试)"
    Start-Sleep 15
}

if (-not $dockerOk) {
    Write-Err "Docker 启动失败，请手动打开 Docker Desktop"
    Write-Warn "Docker 就绪后再次运行此脚本"
    Read-Host "按回车退出"
    exit 1
}

# ===== 第3步：配置 Docker 镜像加速 =====
Write-Step "第3步：配置 Docker 镜像加速"
$dockerDir = "$env:USERPROFILE\.docker"
if (-not (Test-Path $dockerDir)) { New-Item -ItemType Directory -Path $dockerDir -Force | Out-Null }
$daemonConfig = @'
{
  "builder": { "gc": { "defaultKeepStorage": "20GB", "enabled": true } },
  "experimental": false,
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.xuanyuan.me",
    "https://docker.m.daocloud.io"
  ]
}
'@
$daemonConfig | Out-File -FilePath "$dockerDir\daemon.json" -Encoding utf8
Write-OK "镜像加速已配置，重启 Docker..."
Stop-Process -Name "Docker Desktop" -Force -ErrorAction SilentlyContinue
Start-Sleep 5
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
Start-Sleep 30

# 等待 Docker 重新就绪
for ($i = 0; $i -lt 12; $i++) {
    try {
        $result = docker info 2>&1
        if ($result -match "Server Version") {
            Write-OK "Docker 重启完成"
            break
        }
    } catch { }
    Start-Sleep 10
}

# ===== 第4步：准备项目代码 =====
Write-Step "第4步：准备项目代码"
$appDir = "C:\video-app"
if (-not (Test-Path $appDir)) {
    New-Item -ItemType Directory -Path $appDir -Force | Out-Null
    cd C:\
    git clone https://gitee.com/ljl-one/video-app.git $appDir
    Write-OK "代码已克隆"
} else {
    cd $appDir
    git pull origin main 2>$null
    Write-OK "代码已更新"
}

# ===== 第5步：构建前端 =====
Write-Step "第5步：构建前端"
cd "$appDir\video-front"
if (-not (Test-Path "dist\index.html")) {
    npm install --registry https://registry.npmmirror.com
    npm run build
    Write-OK "前端构建完成"
} else {
    Write-OK "前端已构建，跳过"
}

# ===== 第6步：构建后端 jar =====
Write-Step "第6步：构建后端 jar"
cd "$appDir\video-project"
if (-not (Test-Path "target\video-server-0.0.1-SNAPSHOT.jar")) {
    # 用 Docker 构建，避免安装 JDK
    docker run --rm -v "${appDir}\video-project:/app" -w /app maven:3.9-eclipse-temurin-17 mvn clean package -DskipTests
    Write-OK "后端构建完成"
} else {
    Write-OK "jar 已存在，跳过"
}

# 验证 jar
$jarFile = Get-ChildItem "$appDir\video-project\target\*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $jarFile) {
    Write-Err "jar 包不存在，构建失败"
    Read-Host "按回车退出"
    exit 1
}
Write-OK "jar 包: $($jarFile.Name)"

# ===== 第7步：创建 .env 文件 =====
Write-Step "第7步：创建环境配置文件"
$envContent = @'
# MySQL
MYSQL_ROOT_PASSWORD=Video@2026
MYSQL_DATABASE=video_meeting

# Redis
REDIS_PASSWORD=

# RabbitMQ
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin
MINIO_BUCKET=video-meeting

# JWT
JWT_SECRET=YourSuperSecretKeyForJwtTokenGenerationMustBeAtLeast32Chars2026

# 镜像前缀（留空用 Docker Hub）
IMAGE_REGISTRY_PREFIX=
'@
$envContent | Out-File -FilePath "$appDir\.env" -Encoding utf8
Write-OK ".env 已创建"

# ===== 第8步：启动 Docker 服务 =====
Write-Step "第8步：启动 Docker 服务"
cd $appDir
$env:MINIO_PUBLIC_BASE_URL = "http://59.110.171.175:9000/video-meeting"

docker compose -f docker-compose-prod.yml --env-file .env down 2>$null
docker compose -f docker-compose-prod.yml --env-file .env up -d

Write-Warn "等待服务启动 90 秒..."
Start-Sleep 90

# ===== 第9步：开放防火墙端口 =====
Write-Step "第9步：开放 Windows 防火墙端口"
$ports = @(80, 8081, 9000, 9001)
foreach ($port in $ports) {
    $ruleName = "Video App $port"
    netsh advfirewall firewall delete rule name=$ruleName 2>$null
    netsh advfirewall firewall add rule name=$ruleName dir=in action=allow protocol=TCP localport=$port | Out-Null
    Write-OK "端口 $port 已开放"
}

# ===== 第10步：验证服务 =====
Write-Step "第10步：验证服务"

Write-Host "`n--- 容器状态 ---" -ForegroundColor Cyan
docker ps

Write-Host "`n--- 后端健康检查 ---" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/health" -UseBasicParsing -TimeoutSec 10
    Write-OK "后端: $($response.Content)"
} catch {
    Write-Err "后端未就绪: $($_.Exception.Message)"
}

Write-Host "`n--- 前端检查 ---" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost" -UseBasicParsing -TimeoutSec 10
    Write-OK "前端: HTTP $($response.StatusCode)"
} catch {
    Write-Err "前端未就绪: $($_.Exception.Message)"
}

# ===== 完成 =====
Write-Host "`n================================================" -ForegroundColor Green
Write-Host "  部署完成！" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host "`n访问地址:" -ForegroundColor Yellow
Write-Host "  前端: http://59.110.171.175" -ForegroundColor White
Write-Host "  API:  http://59.110.171.175/api" -ForegroundColor White
Write-Host "  MinIO: http://59.110.171.175:9001" -ForegroundColor White
Write-Host "`n登录账号:" -ForegroundColor Yellow
Write-Host "  用户名: admin" -ForegroundColor White
Write-Host "  密码:   123456" -ForegroundColor White
Write-Host "`n注意: 请确保阿里云安全组已开放 80/8081/9000/9001 端口" -ForegroundColor Red
Write-Host "================================================`n" -ForegroundColor Green

Write-Host "按回车键退出..." -ForegroundColor Yellow
Read-Host
