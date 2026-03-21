#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    毕业设计项目 - 停止服务${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Stop backend
stop_backend() {
    echo -e "${YELLOW}正在停止后端服务...${NC}"

    # Find process running mvn spring-boot:run
    BACKEND_PIDS=$(pgrep -f "mvn spring-boot:run" | grep -v grep)

    if [ -z "$BACKEND_PIDS" ]; then
        echo -e "${YELLOW}⚠ 后端服务未运行${NC}"
    else
        for PID in $BACKEND_PIDS; do
            kill $PID 2>/dev/null
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}✓ 后端服务已停止 (PID: $PID)${NC}"
            else
                echo -e "${RED}✗ 停止后端服务失败 (PID: $PID)${NC}"
            fi
        done
    fi
    echo ""
}

# Stop frontend
stop_frontend() {
    echo -e "${YELLOW}正在停止前端服务...${NC}"

    # Find process running npm run dev
    FRONTEND_PIDS=$(pgrep -f "npm run dev" | grep -v grep)

    if [ -z "$FRONTEND_PIDS" ]; then
        echo -e "${YELLOW}⚠ 前端服务未运行${NC}"
    else
        for PID in $FRONTEND_PIDS; do
            kill $PID 2>/dev/null
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}✓ 前端服务已停止 (PID: $PID)${NC}"
            else
                echo -e "${RED}✗ 停止前端服务失败 (PID: $PID)${NC}"
            fi
        done
    fi
    echo ""
}

# Stop all node processes (for frontend)
stop_all_node() {
    echo -e "${YELLOW}正在清理 Node.js 进程...${NC}"

    NODE_PIDS=$(pgrep -f "node" | grep -v grep)

    if [ ! -z "$NODE_PIDS" ]; then
        for PID in $NODE_PIDS; do
            kill $PID 2>/dev/null
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}✓ 已清理进程 (PID: $PID)${NC}"
            fi
        done
    fi
    echo ""
}

# Main execution
main() {
    stop_backend
    stop_frontend

    # Optional: uncomment the following line to also stop all node processes
    # stop_all_node

    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}    所有服务已停止！${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""

    echo -e "${BLUE}当前运行的相关进程:${NC}"
    echo -e "  ${YELLOW}Maven 进程:${NC}"
    pgrep -f "mvn" | wc -l | xargs echo "    数量:"

    echo -e "  ${YELLOW}Node.js 进程:${NC}"
    pgrep -f "node" | wc -l | xargs echo "    数量:"
    echo ""
}

# Run main function
main

