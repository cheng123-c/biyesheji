#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Constants
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BE_DIR="$PROJECT_DIR/be"
FE_DIR="$PROJECT_DIR/fe"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    毕业设计项目 - 生产启动${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Start backend in background
start_backend() {
    echo -e "${YELLOW}正在启动后端服务...${NC}"
    cd "$BE_DIR"
    nohup mvn spring-boot:run > /tmp/backend.log 2>&1 &
    BACKEND_PID=$!
    echo -e "${GREEN}✓ 后端服务已启动 (PID: $BACKEND_PID)${NC}"
    echo "  日志文件: /tmp/backend.log"
    echo ""
}

# Start frontend in background
start_frontend() {
    echo -e "${YELLOW}正在启动前端服务...${NC}"
    cd "$FE_DIR"
    nohup npm run dev > /tmp/frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo -e "${GREEN}✓ 前端服务已启动 (PID: $FRONTEND_PID)${NC}"
    echo "  日志文件: /tmp/frontend.log"
    echo ""
}

# Main execution
main() {
    start_backend
    start_frontend

    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}    所有服务已启动！${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""

    echo -e "${BLUE}访问地址:${NC}"
    echo -e "  ${YELLOW}前端:${NC} http://localhost:3000"
    echo -e "  ${YELLOW}后端 API:${NC} http://localhost:8080/api"
    echo -e "  ${YELLOW}健康检查:${NC} http://localhost:8080/api/health"
    echo ""

    echo -e "${BLUE}日志查看:${NC}"
    echo -e "  ${YELLOW}后端:${NC} tail -f /tmp/backend.log"
    echo -e "  ${YELLOW}前端:${NC} tail -f /tmp/frontend.log"
    echo ""

    echo -e "${BLUE}停止服务:${NC}"
    if [ ! -z "$BACKEND_PID" ]; then
        echo -e "  ${YELLOW}后端:${NC} kill $BACKEND_PID"
    fi
    if [ ! -z "$FRONTEND_PID" ]; then
        echo -e "  ${YELLOW}前端:${NC} kill $FRONTEND_PID"
    fi
    echo ""
}

# Run main function
main

