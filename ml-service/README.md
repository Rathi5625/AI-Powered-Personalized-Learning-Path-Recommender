# AI Personalized Learning Path - Machine Learning Service

Standalone Python microservice built with **FastAPI**, **Scikit-Learn**, **Pandas**, and **NumPy** for machine learning recommendation model training and inference.

---

## Service Overview

- **Service Name**: `learning-path-ml-service`
- **Framework**: FastAPI + Uvicorn
- **Default Port**: `8000`
- **Root Endpoint**: `GET /`
- **Health Endpoint**: `GET /health`

---

## Setup & Local Execution

### 1. Create Virtual Environment
```bash
cd ml-service
python -m venv .venv
```

Activate the environment:
- **Windows (PowerShell)**:
  ```powershell
  .\.venv\Scripts\Activate.ps1
  ```
- **Linux / macOS**:
  ```bash
  source .venv/bin/activate
  ```

### 2. Install Dependencies
```bash
pip install -r requirements.txt
```

### 3. Verify ML Training Environment
```bash
python training/train.py
```
*Expected Output*:
```text
Verifying ML training environment dependencies...
Pandas version: ...
NumPy version: ...
Scikit-Learn version: ...
Joblib version: ...
ML training environment is ready.
```

### 4. Start FastAPI Service
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

---

## API Endpoints

### Health Check
- `GET /health`
```json
{
  "status": "UP",
  "service": "learning-path-ml-service"
}
```

### Service Meta Info
- `GET /`
```json
{
  "service": "AI Personalized Learning Path ML Service",
  "version": "1.0.0"
}
```

---

## Model Training (Next Steps)
Model training pipeline and dataset generation scripts will be added in Step 9B.
The trained models will be exported to the `models/` directory using `joblib`.
