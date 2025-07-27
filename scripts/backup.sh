#!/bin/bash

# MySQL Docker 데이터 백업 및 Private Git Repo Push 스크립트
echo "🔄 Starting MySQL backup process..."

# 스크립트 디렉토리 기준으로 프로젝트 루트로 이동
cd "$(dirname "$0")/.."

# 설정 변수 - 여기서 data repo URL을 설정하세요
DATA_REPO_URL="git@github.com:ch3224bin/mud-mk2-db-data.git"  # 여기를 수정하세요
DATA_REPO_DIR="mysql-data-repo"
CONTAINER_NAME="mud-mk2-mysql"
DB_PASSWORD="rootpassword"
BACKUP_FILE="init/01-init-data.sql"

echo "📂 Working directory: $(pwd)"

# MySQL 컨테이너가 실행 중인지 확인
if ! docker ps | grep -q $CONTAINER_NAME; then
    echo "❌ MySQL container '$CONTAINER_NAME' is not running!"
    echo "   Please start the container first: docker-compose up -d"
    exit 1
fi

# Data repo 디렉토리 확인 및 설정
if [ ! -d "$DATA_REPO_DIR" ]; then
    echo "📂 Data repository not found. Cloning..."
    git clone $DATA_REPO_URL $DATA_REPO_DIR
    if [ $? -ne 0 ]; then
        echo "❌ Failed to clone data repository!"
        echo "   Please check the repository URL: $DATA_REPO_URL"
        exit 1
    fi
else
    echo "📥 Pulling latest data repository..."
    cd $DATA_REPO_DIR
    git pull
    cd ..
fi

# init 디렉토리 생성
mkdir -p $DATA_REPO_DIR/init

# 데이터베이스 덤프
echo "📦 Creating database dump..."
docker exec $CONTAINER_NAME mysqldump -u root -p$DB_PASSWORD --all-databases --routines --triggers > $DATA_REPO_DIR/$BACKUP_FILE

if [ $? -eq 0 ]; then
    echo "✅ Database dump created successfully!"
else
    echo "❌ Failed to create database dump!"
    exit 1
fi

# Data repo로 이동하여 Git 작업
cd $DATA_REPO_DIR

# Git add, commit, push
echo "📤 Pushing to data repository..."
git add $BACKUP_FILE
git add init/

# 커밋 메시지에 타임스탬프 포함
TIMESTAMP=$(date "+%Y-%m-%d %H:%M:%S")
git commit -m "Database backup - $TIMESTAMP"

if [ $? -eq 0 ]; then
    echo "✅ Changes committed successfully!"

    # Push to remote
    git push

    if [ $? -eq 0 ]; then
        echo "🎉 Database backup completed and pushed to private data repository!"
        echo "📁 Backup file: $DATA_REPO_DIR/$BACKUP_FILE"
    else
        echo "⚠️  Commit successful, but push failed. Please check your Git remote configuration."
    fi
else
    echo "ℹ️  No changes to commit (database might be unchanged)"
fi

# 원래 디렉토리로 돌아가기
cd ..

echo "✨ Backup process finished!"