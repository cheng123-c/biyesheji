#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PID_DIR="/tmp/biyesheji"
BACKEND_PID_FILE="$PID_DIR/backend.pid"
FRONTEND_PID_FILE="$PID_DIR/frontend.pid"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    毕业设计项目 - 停止服务${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 通用：kill 一个进程树（父进程 + 所有子进程）
kill_tree() {
    local ROOT_PID=$1
    local SIG=${2:-TERM}
    # 找出所有子孙进程
    local CHILDREN
    CHILDREN=$(pgrep -P "$ROOT_PID" 2>/dev/null)
    for CHILD in $CHILDREN; do
        kill_tree "$CHILD" "$SIG"
    done
    kill "-$SIG" "$ROOT_PID" 2>/dev/null
}

# 等待进程真正退出，超时后强制 SIGKILL
wait_for_exit() {
    local PID=$1
    local NAME=$2
    local TIMEOUT=10
    local COUNT=0
    while kill -0 "$PID" 2>/dev/null; do
        sleep 1
        COUNT=$((COUNT + 1))
        if [ $COUNT -ge $TIMEOUT ]; then
            echo -e "${YELLOW}  ⚠ $NAME 未在 ${TIMEOUT}s 内退出，强制 SIGKILL...${NC}"
            kill_tree "$PID" KILL
            sleep 1
            return
        fi
    done
}

# 停止后端
stop_backend() {
    echo -e "${YELLOW}正在停止后端服务...${NC}"

    local KILLED=0

    # 优先使用 PID 文件
    if [ -f "$BACKEND_PID_FILE" ]; then
        local PID
        PID=$(cat "$BACKEND_PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            echo -e "  停止进程树 (root PID: $PID)..."
            kill_tree "$PID" TERM
            wait_for_exit "$PID" "后端"
            echo -e "${GREEN}  ✓ 后端服务已停止${NC}"
            KILLED=1
        fi
        rm -f "$BACKEND_PID_FILE"
    fi

    # 兜底：用端口 + 进程名双重匹配，避免漏掉残留进程
    # 查找占用 8080 端口的进程
    local PORT_PIDS
    PORT_PIDS=$(lsof -ti tcp:8080 2>/dev/null)
    for PID in $PORT_PIDS; do
        echo -e "  清理残留进程 (PID: $PID, 占用端口 8080)..."
        kill_tree "$PID" TERM
        wait_for_exit "$PID" "后端残留"
        KILLED=1
    done

    # 查找残留的 Spring Boot / Maven 进程
    local MVN_PIDS
    MVN_PIDS=$(pgrep -f "spring-boot:run" 2>/dev/null)
    for PID in $MVN_PIDS; do
        echo -e "  清理残留 Maven 进程 (PID: $PID)..."
        kill_tree "$PID" TERM
        wait_for_exit "$PID" "Maven"
        KILLED=1
    done

    if [ $KILLED -eq 0 ]; then
        echo -e "${YELLOW}  ⚠ 后端服务未运行${NC}"
    else
        echo -e "${GREEN}  ✓ 后端已完全停止${NC}"
    fi
    echo ""
}

# 停止前端
stop_frontend() {
    echo -e "${YELLOW}正在停止前端服务...${NC}"

    local KILLED=0

    # 优先使用 PID 文件
    if [ -f "$FRONTEND_PID_FILE" ]; then
        local PID
        PID=$(cat "$FRONTEND_PID_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            echo -e "  停止进程树 (root PID: $PID)..."
            kill_tree "$PID" TERM
            wait_for_exit "$PID" "前端"
            echo -e "${GREEN}  ✓ 前端服务已停止${NC}"
            KILLED=1
        fi
        rm -f "$FRONTEND_PID_FILE"
    fi

    # 兜底：查找占用 3000 端口的进程
    local PORT_PIDS
    PORT_PIDS=$(lsof -ti tcp:3000 2>/dev/null)
    for PID in $PORT_PIDS; do
        echo -e "  清理残留进程 (PID: $PID, 占用端口 3000)..."
        kill_tree "$PID" TERM
        wait_for_exit "$PID" "前端残留"
        KILLED=1
    done

    # 查找残留的 Vite dev server 进程（精确匹配，不误杀其他 node）
    local VITE_PIDS
    VITE_PIDS=$(pgrep -f "vite" 2>/dev/null)
    for PID in $VITE_PIDS; do
        echo -e "  清理残留 Vite 进程 (PID: $PID)..."
        kill_tree "$PID" TERM
        wait_for_exit "$PID" "Vite"
        KILLED=1
    done

    if [ $KILLED -eq 0 ]; then
        echo -e "${YELLOW}  ⚠ 前端服务未运行${NC}"
    else
        echo -e "${GREEN}  ✓ 前端已完全停止${NC}"
    fi
    echo ""
}

# 主流程
main() {
    stop_backend
    stop_frontend

    # 清理空 PID 目录
    rmdir "$PID_DIR" 2>/dev/null

    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}    所有服务已停止！${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""

    # 验证端口是否已释放
    echo -e "${BLUE}端口占用检查:${NC}"
    if lsof -ti tcp:8080 &>/dev/null; then
        echo -e "  ${RED}✗ 端口 8080 仍被占用${NC}"
        lsof -ti tcp:8080 | xargs -I{} sh -c 'echo "    PID: {} ($(ps -p {} -o comm= 2>/dev/null))"'
    else
        echo -e "  ${GREEN}✓ 端口 8080 已释放${NC}"
    fi

    if lsof -ti tcp:3000 &>/dev/null; then
        echo -e "  ${RED}✗ 端口 3000 仍被占用${NC}"
        lsof -ti tcp:3000 | xargs -I{} sh -c 'echo "    PID: {} ($(ps -p {} -o comm= 2>/dev/null))"'
    else
        echo -e "  ${GREEN}✓ 端口 3000 已释放${NC}"
    fi
    echo ""
}

main

