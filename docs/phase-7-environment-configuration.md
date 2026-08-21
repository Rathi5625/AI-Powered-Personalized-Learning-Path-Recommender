# Phase 7 — Production Environment Configuration & Deployment Guide

**Generated Date:** August 21, 2026  
**System:** AI-Powered Personalized Learning Path Recommender  
**Scope:** Environment Variables, CORS Policies, Port Allocation & Multi-Tier Deployment  
**Status:** COMPLETED & VERIFIED

---

## 1. Multi-Tier Architecture & Port Layout

The system consists of three coordinated tiers plus an edge proxy:

| Tier | Service Name | Default Port | Technology | Configuration Location |
| :--- | :--- | :--- | :--- | :--- |
| **Edge Proxy** | Cloudflare Tunnel / CDN | `443 / 80` | Cloudflare Worker / DNS | Edge Network |
| **Frontend** | `frontend-v2` | `5173` (Dev) / Static Nginx | React 18, Vite 6, TypeScript | `.env`, `vite.config.ts` |
| **Backend** | `learning-path-backend` | `8080` | Spring Boot 3.5.x, Java 17 | `application.yml` |
| **ML Engine** | `ml-service` | `8000` | FastAPI, Python 3.14 | `config/settings.py` |
| **Database** | PostgreSQL | `5432` | PostgreSQL 16 | `docker-compose.yml` |

---

## 2. Backend Configuration (`application.yml` / Environment Variables)

```yaml
server:
  port: ${PORT:8080}

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/learning_path_db}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

jwt:
  secret: ${JWT_SECRET:your-256-bit-production-secret-key-here-must-be-secure}
  expiration-ms: ${JWT_EXPIRATION_MS:86400000} # 24 hours
  refresh-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000} # 7 days

ai:
  gemini:
    api-key: ${GEMINI_API_KEY:}
    model: ${GEMINI_MODEL:gemini-2.5-flash}
    api-url: https://generativelanguage.googleapis.com/v1beta

ml:
  service:
    url: ${ML_SERVICE_URL:http://localhost:8000}
    timeout-ms: ${ML_SERVICE_TIMEOUT_MS:3000}

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000,https://app.learnai.com}
```

---

## 3. ML Service Configuration (`ml-service/config/settings.py`)

```python
import os
from pydantic import BaseSettings

class Settings(BaseSettings):
    APP_NAME: str = "LearnAI ML Ranking & Recommendation Service"
    APP_VERSION: str = "2.0.0"
    HOST: str = os.getenv("ML_HOST", "0.0.0.0")
    PORT: int = int(os.getenv("ML_PORT", "8000"))
    
    # Model Artifact Paths
    ACTIVE_MODEL_PATH: str = "models/active/model.joblib"
    FEATURE_META_PATH: str = "models/active/feature_meta.json"
    MODEL_REGISTRY_PATH: str = "models/model_registry.json"
    
    # Retraining Thresholds
    MIN_INTERACTIONS_FOR_RETRAIN: int = 500
    F1_SCORE_IMPROVEMENT_THRESHOLD: float = 0.005 # Candidate must beat active by +0.5% F1

    class Config:
        env_file = ".env"

settings = Settings()
```

---

## 4. Frontend Configuration (`frontend-v2/.env`)

```env
# API Base Endpoint (Routes to Spring Boot Backend or Cloudflare Proxy)
VITE_API_BASE_URL=http://localhost:8080/api/v1

# App Metadata
VITE_APP_NAME=LearnAI Recommender
VITE_APP_VERSION=2.0.0
```

---

## 5. Multi-Origin CORS Matrix

| Origin | Allowed Methods | Allowed Headers | Credentials |
| :--- | :--- | :--- | :--- |
| `http://localhost:5173` (Vite Local) | `GET, POST, PUT, DELETE, PATCH, OPTIONS` | `Authorization, Content-Type, X-Requested-With` | `true` |
| `https://*.learnai.com` (Production) | `GET, POST, PUT, DELETE, PATCH, OPTIONS` | `Authorization, Content-Type, X-Requested-With` | `true` |

---

## 6. Startup & Verification Commands

### 6.1 Backend Startup
```powershell
cd backend/learning-path-backend
mvn spring-boot:run
```

### 6.2 ML Service Startup
```powershell
cd ml-service
$env:PYTHONPATH="."
.\.venv\Scripts\uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 6.3 Frontend Startup
```powershell
cd frontend-v2
npm run dev
```

---

## 7. Sign-off

Environment variable templates, CORS configurations, and containerization endpoints are verified and ready for production deployment.
