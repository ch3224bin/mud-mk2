# MySQL Docker 데이터 동기화 가이드

## 📁 프로젝트 구조
```
your-project/
├── scripts/
│   ├── backup.sh             # 데이터 백업
│   ├── sync.sh               # 데이터 동기화  
│   └── setup-data-repo.sh    # 초기 설정
├── mysql-data-repo/          # Private Git repo (자동 생성)
│   └── init/
│       └── 01-init-data.sql
├── docker-compose.yml
├── .gitignore               # mysql-data-repo/ 제외
└── src/
```

## 🚀 초기 설정 (한 번만)

### 1단계: Private Repository 생성
- GitHub에서 새 Private Repository 생성 (예: `mysql-data-private`)
- SSH 클론 URL 복사 (`git@github.com:username/mysql-data-private.git`)

### 2단계: SSH 키 설정 (필요시)
```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
ssh-add ~/.ssh/id_ed25519
cat ~/.ssh/id_ed25519.pub  # GitHub에 등록
```

### 3단계: 스크립트 설정
```bash
# 1. scripts 디렉토리에 파일들 저장
mkdir scripts

# 2. 실행 권한 설정
chmod +x scripts/*.sh

# 3. setup-data-repo.sh에서 DATA_REPO_URL 수정
# 4. 초기 설정 실행
./scripts/setup-data-repo.sh

# 5. backup.sh, sync.sh에서도 동일한 URL로 수정
```

### 4단계: .gitignore 설정
프로젝트 루트에 추가:
```gitignore
mysql-data-repo/
```

## 💻 IDE 설정

### IntelliJ IDEA
**Run → Edit Configurations → "+" → Shell Script**

| 이름 | Script Path | Working Directory |
|------|-------------|-------------------|
| MySQL Backup | `scripts/backup.sh` | `$ProjectFileDir$` |
| MySQL Sync | `scripts/sync.sh` | `$ProjectFileDir$` |

### VS Code
`.vscode/tasks.json` 생성:
```json
{
    "version": "2.0.0",
    "tasks": [
        {
            "label": "MySQL Backup",
            "type": "shell",
            "command": "./scripts/backup.sh",
            "group": "build"
        },
        {
            "label": "MySQL Sync", 
            "type": "shell",
            "command": "./scripts/sync.sh",
            "group": "build"
        }
    ]
}
```

## 📝 사용법

### 개발 완료 후 (데이터 백업)
```bash
./scripts/backup.sh
```
- MySQL 데이터 덤프 생성
- Private repo에 자동 커밋/푸시

### 새 환경에서 개발 시작 (데이터 동기화)
```bash
./scripts/sync.sh  
```
- Private repo에서 최신 데이터 가져오기
- MySQL 컨테이너 재시작 (백업 데이터로 초기화)

### 팀 개발 워크플로우
1. **개발 시작**: `./scripts/sync.sh`
2. **개발 진행** (데이터 변경)
3. **작업 완료**: `./scripts/backup.sh`
4. **다른 팀원**: `./scripts/sync.sh`

## ⚙️ 설정 변경

### 주요 설정 파일
- **scripts/backup.sh**: `DATA_REPO_URL`, `DB_PASSWORD`
- **scripts/sync.sh**: `DATA_REPO_URL`
- **docker-compose.yml**: `MYSQL_ROOT_PASSWORD`, 포트

### MySQL 접속 정보
- Host: `localhost:3306`
- Username: `root`
- Password: `devpassword`

## 🔧 문제 해결

### 권한 오류
```bash
chmod +x scripts/*.sh
```

### SSH 연결 문제
```bash
ssh -T git@github.com
ssh-add -l
```

### 컨테이너 문제
```bash
docker-compose logs mysql
docker-compose ps
```

### 완전 초기화
```bash
docker-compose down -v
rm -rf mysql-data-repo
./scripts/sync.sh
```

## 🖥️ Windows WSL

WSL에서는 Linux 스크립트를 그대로 사용:
```bash
./scripts/backup.sh
./scripts/sync.sh
```

IntelliJ에서 WSL 터미널을 기본으로 설정하면 동일하게 사용 가능합니다.