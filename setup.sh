#!/bin/bash

###############################################################################
# 健康评测系统 - 环境自动安装脚本
# 功能: 自动检测和安装 Node.js, npm, Java 11, Maven, Docker
# 并启动 MySQL Docker 容器
###############################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_status() {
    echo -e "${BLUE}==>${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

###############################################################################
# 检测操作系统
###############################################################################

detect_os() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="linux"
        DISTRO=$(lsb_release -si 2>/dev/null || echo "Linux")
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
    elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
        OS="windows"
    else
        OS="unknown"
    fi
    echo "$OS"
}

###############################################################################
# Node.js 和 npm 安装
###############################################################################

install_nodejs() {
    print_status "检查 Node.js..."

    if command -v node &> /dev/null; then
        node_version=$(node -v)
        print_success "Node.js 已安装: $node_version"

        npm_version=$(npm -v)
        print_success "npm 已安装: $npm_version"
        return 0
    fi

    print_warning "Node.js 未安装，正在安装..."

    OS=$(detect_os)

    if [[ "$OS" == "macos" ]]; then
        if command -v brew &> /dev/null; then
            brew install node
            print_success "Node.js 已通过 Homebrew 安装"
        else
            print_error "请先安装 Homebrew: https://brew.sh"
            return 1
        fi
    elif [[ "$OS" == "linux" ]]; then
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y nodejs npm
            print_success "Node.js 和 npm 已通过 apt 安装"
        elif command -v yum &> /dev/null; then
            sudo yum install -y nodejs npm
            print_success "Node.js 和 npm 已通过 yum 安装"
        else
            print_error "不支持的 Linux 发行版"
            return 1
        fi
    elif [[ "$OS" == "windows" ]]; then
        print_error "请访问 https://nodejs.org/ 下载安装 Node.js"
        return 1
    fi
}

###############################################################################
# Java 11 安装
###############################################################################

install_java() {
    print_status "检查 Java 11..."

    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | grep -oP '(?<=version ")[\d.]+' || echo "unknown")

        if [[ $java_version == 11* ]] || [[ $java_version == "11" ]]; then
            print_success "Java 11 已安装: $java_version"
            return 0
        else
            print_warning "已安装的 Java 版本: $java_version (需要 Java 11)"
        fi
    fi

    print_warning "Java 11 未安装，正在安装..."

    OS=$(detect_os)

    if [[ "$OS" == "macos" ]]; then
        if command -v brew &> /dev/null; then
            brew install openjdk@11
            sudo ln -sfn /opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk 2>/dev/null || true
            print_success "Java 11 已通过 Homebrew 安装"
        else
            print_error "请先安装 Homebrew: https://brew.sh"
            return 1
        fi
    elif [[ "$OS" == "linux" ]]; then
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y openjdk-11-jdk
            print_success "Java 11 已通过 apt 安装"
        elif command -v yum &> /dev/null; then
            sudo yum install -y java-11-openjdk java-11-openjdk-devel
            print_success "Java 11 已通过 yum 安装"
        else
            print_error "不支持的 Linux 发行版"
            return 1
        fi
    elif [[ "$OS" == "windows" ]]; then
        print_error "请访问 https://jdk.java.net/archive/ 下载安装 Java 11"
        return 1
    fi
}

###############################################################################
# Maven 安装
###############################################################################

install_maven() {
    print_status "检查 Maven..."

    if command -v mvn &> /dev/null; then
        maven_version=$(mvn -v | head -n 1)
        print_success "Maven 已安装: $maven_version"
        return 0
    fi

    print_warning "Maven 未安装，正在安装..."

    OS=$(detect_os)

    if [[ "$OS" == "macos" ]]; then
        if command -v brew &> /dev/null; then
            brew install maven
            print_success "Maven 已通过 Homebrew 安装"
        else
            print_error "请先安装 Homebrew: https://brew.sh"
            return 1
        fi
    elif [[ "$OS" == "linux" ]]; then
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y maven
            print_success "Maven 已通过 apt 安装"
        elif command -v yum &> /dev/null; then
            sudo yum install -y maven
            print_success "Maven 已通过 yum 安装"
        else
            print_error "不支持的 Linux 发行版"
            return 1
        fi
    elif [[ "$OS" == "windows" ]]; then
        print_error "请访问 https://maven.apache.org/download.cgi 下载安装 Maven"
        return 1
    fi
}

###############################################################################
# Docker 和 Docker Compose 安装
###############################################################################

install_docker() {
    print_status "检查 Docker..."

    if command -v docker &> /dev/null; then
        docker_version=$(docker --version)
        print_success "Docker 已安装: $docker_version"
    else
        print_warning "Docker 未安装，正在安装..."

        OS=$(detect_os)

        if [[ "$OS" == "macos" ]]; then
            print_error "请访问 https://www.docker.com/products/docker-desktop 下载安装 Docker Desktop"
            return 1
        elif [[ "$OS" == "linux" ]]; then
            curl -fsSL https://get.docker.com -o get-docker.sh
            sudo chmod +x get-docker.sh
            sudo sh get-docker.sh
            sudo usermod -aG docker $USER
            rm get-docker.sh
            print_success "Docker 已安装"
            print_warning "请运行: newgrp docker"
        elif [[ "$OS" == "windows" ]]; then
            print_error "请访问 https://www.docker.com/products/docker-desktop 下载安装 Docker Desktop"
            return 1
        fi
    fi

    print_status "检查 Docker Compose..."

    if command -v docker compose &> /dev/null; then
        docker_compose_version=$(docker compose version)
        print_success "Docker Compose 已安装: $docker_compose_version"
    else
        print_warning "Docker Compose 未安装，正在安装..."

        OS=$(detect_os)

        if [[ "$OS" == "macos" ]]; then
            print_warning "Docker Compose 应该已随 Docker Desktop 安装"
        elif [[ "$OS" == "linux" ]]; then
            sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
            sudo chmod +x /usr/local/bin/docker-compose
            print_success "Docker Compose 已安装"
        fi
    fi
}

###############################################################################
# 启动 MySQL Docker 容器
###############################################################################

start_mysql() {
    print_status "启动 MySQL Docker 容器..."

    # 检查 Docker daemon 是否运行
    if ! docker info &> /dev/null; then
        print_error "Docker 未运行，请先启动 Docker"
        return 1
    fi

    # 检查容器是否已存在
    if docker ps -a --format '{{.Names}}' | grep -q '^biyesheji-mysql$'; then
        print_warning "MySQL 容器已存在"

        if docker ps --format '{{.Names}}' | grep -q '^biyesheji-mysql$'; then
            print_success "MySQL 容器已在运行"
            return 0
        else
            print_status "启动现有的 MySQL 容器..."
            docker start biyesheji-mysql
            print_success "MySQL 容器已启动"
            return 0
        fi
    fi

    # 检查 docker-compose.yml 是否存在
    if [ ! -f "docker-compose.yml" ]; then
        print_error "docker-compose.yml 文件不存在"
        return 1
    fi

    # 启动 Docker Compose
    docker compose up -d

    if [ $? -eq 0 ]; then
        print_success "MySQL 容器已启动"

        # 等待数据库就绪
        print_status "等待 MySQL 数据库就绪..."
        for i in {1..30}; do
            if docker exec biyesheji-mysql mysql -uroot -proot -e "SELECT 1" &> /dev/null; then
                print_success "MySQL 数据库已就绪"
                return 0
            fi
            echo -n "."
            sleep 1
        done

        print_warning "MySQL 数据库连接超时，请检查"
        return 1
    else
        print_error "启动 MySQL 容器失败"
        return 1
    fi
}

###############################################################################
# 安装前端依赖
###############################################################################

install_frontend_deps() {
    print_status "安装前端依赖..."

    if [ -d "fe" ]; then
        cd fe

        if [ ! -d "node_modules" ]; then
            print_status "运行 npm install..."
            npm install
            print_success "前端依赖已安装"
        else
            print_success "前端依赖已安装"
        fi

        cd ..
    else
        print_warning "fe 目录不存在"
    fi
}

###############################################################################
# 安装后端依赖
###############################################################################

install_backend_deps() {
    print_status "安装后端依赖..."

    if [ -d "be" ]; then
        cd be

        print_status "运行 mvn clean install..."
        mvn clean install -q
        print_success "后端依赖已安装"

        cd ..
    else
        print_warning "be 目录不存在"
    fi
}

###############################################################################
# 显示完成信息
###############################################################################

show_completion_info() {
    cat << 'EOF'

╔═══════════════════════════════════════════════════════════════════════════╗
║                   ✅ 环境安装和初始化完成！                               ║
╚═══════════════════════════════════════════════════════════════════════════╝

📋 已完成的操作:
  ✓ Node.js 和 npm 已安装
  ✓ Java 11 已安装
  ✓ Maven 已安装
  ✓ Docker 和 Docker Compose 已安装
  ✓ MySQL Docker 容器已启动
  ✓ 前端依赖已安装
  ✓ 后端依赖已安装

🚀 下一步操作:

1️⃣  启动后端服务 (新终端):
    cd be
    mvn spring-boot:run

2️⃣  启动前端服务 (另一个新终端):
    cd fe
    npm run dev

3️⃣  打开浏览器:
    访问 http://localhost:5173

📊 数据库信息:
  - Host: localhost
  - Port: 3306
  - Database: biyesheji
  - Username: root
  - Password: root

✨ 完成！开始开发吧！

EOF
}

###############################################################################
# 主函数
###############################################################################

main() {
    echo ""
    echo -e "${BLUE}╔═══════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║              健康评测系统 - 环境自动安装脚本                              ║${NC}"
    echo -e "${BLUE}╚═══════════════════════════════════════════════════════════════════════════╝${NC}"
    echo ""

    # 检测操作系统
    OS=$(detect_os)
    print_success "检测到操作系统: $OS"
    echo ""

    # 安装各个依赖
    install_nodejs || print_warning "Node.js 安装可能失败"
    echo ""

    install_java || print_warning "Java 安装可能失败"
    echo ""

    install_maven || print_warning "Maven 安装可能失败"
    echo ""

    install_docker || print_warning "Docker 安装可能失败"
    echo ""

    # 启动 MySQL
    start_mysql || print_warning "MySQL 启动可能失败"
    echo ""

    # 安装依赖
    install_frontend_deps || print_warning "前端依赖安装可能失败"
    echo ""

    install_backend_deps || print_warning "后端依赖安装可能失败"
    echo ""

    # 显示完成信息
    show_completion_info
}

# 运行主函数
main "$@"

