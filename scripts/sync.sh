#!/bin/bash

# MySQL Docker 데이터 동기화 스크립트 (Private Git Repo 사용)
echo "🔄 Starting MySQL data synchronization..."

# 스크립트 디렉토리 기준으로 프로젝트 루트로 이동
cd "$(dirname "$0")/.."

# 설정 변수 - 여기서 data repo URL을 설정하세요
DATA_REPO_URL="git@github.com:ch3224bin/mud-mk2-db-data.git"  # 여기를 수정하세요
DATA_REPO_DIR="mysql-data-repo"
CONTAINER_NAME="mysql"

echo "📂 Working directory: $(pwd)"

# Data repo 클론 또는 업데이트
if [ ! -d "$DATA_REPO_DIR" ]; then
    echo "📂 Data repository not found. Cloning..."
    git clone $DATA_REPO_URL $DATA_REPO_DIR
    if [ $? -ne 0 ]; then
        echo "❌ Failed to clone data repository!"
        echo "   Please check the repository URL: $DATA_REPO_URL"
        echo "   Make sure you have access to the private repository."
        exit 1
    fi
    echo "✅ Successfully cloned data repository!"
else
    echo "📥 Pulling latest data from private repository..."
    cd $DATA_REPO_DIR
    git pull
    if [ $? -ne 0 ]; then
        echo "❌ Failed to pull from data repository!"
        echo "   Please check your Git configuration and network connection."
        exit 1
    fi
    cd ..
    echo "✅ Successfully pulled latest changes from data repository!"
fi

# init 디렉토리가 있는지 확인
if [ ! -d "$DATA_REPO_DIR/init" ]; then
    echo "⚠️  No 'init' directory found in data repository."
    echo "ℹ️  Creating empty init directory. The database will start empty."
    mkdir -p $DATA_REPO_DIR/init
fi

# 기존 컨테이너 정리
echo "🧹 Cleaning up existing containers..."
docker-compose down

# 볼륨도 함께 제거 (완전한 초기화를 위해)
echo "🗑️  Removing existing MySQL data volume..."
docker volume rm $(basename $(pwd))_mysql-data 2>/dev/null || true

# 새로운 컨테이너 시작
echo "🚀 Starting MySQL container with synchronized data..."
docker-compose up -d

if [ $? -eq 0 ]; then
    echo "✅ MySQL container started successfully!"

    # 컨테이너가 완전히 시작될 때까지 대기
    echo "⏳ Waiting for MySQL to initialize..."
    sleep 10

    # 연결 테스트
    echo "🔍 Testing MySQL connection..."
    for i in {1..30}; do
        if docker exec $CONTAINER_NAME mysqladmin ping -h localhost --silent; then
            echo "✅ MySQL is ready!"
            break
        fi
        echo "   Waiting for MySQL to be ready... ($i/30)"
        sleep 2
    done

    if [ $i -eq 30 ]; then
        echo "⚠️  MySQL took longer than expected to start. Please check the container logs:"
        echo "   docker-compose logs mysql"
    fi

    echo "🎉 Data synchronization completed!"
    echo "📊 You can now connect to MySQL at localhost:3306"
    echo "📁 Data source: $DATA_REPO_DIR"

else
    echo "❌ Failed to start MySQL container!"
    echo "   Please check docker-compose.yml and try again."
    exit 1
fi

echo "✨ Synchronization process finished!"