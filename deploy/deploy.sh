#!/bin/bash
# ====================================
# 视界视频社区 - 一键部署脚本
# 适用：CentOS / Ubuntu + Nginx + JDK17
# ====================================

set -e

# ---------- 配置区 ----------
# 服务器域名或 IP
SERVER="your-server-ip"
# SSH 用户
SSH_USER="root"
# 部署目录
DEPLOY_DIR="/opt/video-app"
# ---------- 配置区结束 ----------

echo "===== 视界视频社区 部署开始 ====="

# 1. 构建前端
echo "[1/4] 构建前端..."
cd d:/APP/video-front
npm run build
echo "  前端构建完成: dist/"

# 2. 构建后端
echo "[2/4] 构建后端..."
cd d:/APP/video-project
mvn clean package -DskipTests -q
echo "  后端构建完成: target/*.jar"

# 3. 上传文件到服务器
echo "[3/4] 上传文件到服务器 ${SERVER}..."
ssh ${SSH_USER}@${SERVER} "mkdir -p ${DEPLOY_DIR}/{dist,jar,logs,conf}"

# 上传前端静态文件
scp -r d:/APP/video-front/dist/* ${SSH_USER}@${SERVER}:${DEPLOY_DIR}/dist/

# 上传后端 jar
scp d:/APP/video-project/target/*.jar ${SSH_USER}@${SERVER}:${DEPLOY_DIR}/jar/video-server.jar

# 上传 Nginx 配置
scp d:/APP/deploy/nginx.conf ${SSH_USER}@${SERVER}:${DEPLOY_DIR}/conf/

echo "  文件上传完成"

# 4. 在服务器上启动服务
echo "[4/4] 启动服务..."
ssh ${SSH_USER}@${SERVER} << 'REMOTE'
DEPLOY_DIR="/opt/video-app"

# 安装 Nginx 配置
sudo cp ${DEPLOY_DIR}/conf/nginx.conf /etc/nginx/conf.d/video-app.conf
sudo nginx -t && sudo nginx -s reload

# 停止旧的后端进程
PID=$(pgrep -f "video-server.jar" || true)
if [ -n "$PID" ]; then
    echo "  停止旧进程: $PID"
    kill $PID
    sleep 3
fi

# 启动后端
nohup java -jar ${DEPLOY_DIR}/jar/video-server.jar \
    --spring.profiles.active=prod \
    > ${DEPLOY_DIR}/logs/app.log 2>&1 &

echo "  后端已启动，日志: ${DEPLOY_DIR}/logs/app.log"
REMOTE

echo ""
echo "===== 部署完成 ====="
echo "  前端地址: http://${SERVER}"
echo "  API 地址: http://${SERVER}/api"
echo "  后端日志: ssh ${SSH_USER}@${SERVER} 'tail -f ${DEPLOY_DIR}/logs/app.log'"
