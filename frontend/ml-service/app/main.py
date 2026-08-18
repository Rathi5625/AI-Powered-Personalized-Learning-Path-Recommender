from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException, status
from app.schemas import PredictionRequest, PredictionResponse, ModelInfoResponse
from app.model_service import model_service
from app.model_registry import ModelRegistry

registry = ModelRegistry()

@asynccontextmanager
async def lifespan(app: FastAPI):
    model_service.load_model()
    yield

app = FastAPI(
    title="AI Personalized Learning Path ML Service",
    description="Machine Learning service for course recommendation scoring and candidate ranking.",
    version="1.0.0",
    lifespan=lifespan
)

@app.get("/")
def read_root():
    return {
        "service": "AI Personalized Learning Path ML Service",
        "version": "1.0.0"
    }

@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": "learning-path-ml-service",
        "model_loaded": model_service.is_loaded,
        "active_model": model_service.active_version
    }

@app.get("/model-info", response_model=ModelInfoResponse)
def get_model_info():
    if not model_service.is_loaded:
        raise HTTPException(
            status_code=503,
            detail="ML Model is not loaded on server."
        )
    try:
        return model_service.get_model_info()
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error retrieving model info: {str(e)}"
        )

@app.post("/predict", response_model=PredictionResponse)
def predict_recommendation(request: PredictionRequest):
    if not model_service.is_loaded:
        raise HTTPException(
            status_code=503,
            detail="ML Model is not loaded. Inference unavailable."
        )
    try:
        return model_service.predict(request)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Inference error: {str(e)}"
        )

# Administrative Registry & Promotion Control Endpoints
@app.get("/models")
def list_registered_models():
    return {
        "active_model": registry.get_active_version(),
        "models": registry.list_models()
    }

@app.get("/models/active")
def get_active_model_details():
    try:
        return registry.get_active_model_info()
    except Exception as e:
        raise HTTPException(status_code=404, detail=str(e))

@app.post("/models/{version}/promote")
def promote_candidate_model(version: str):
    try:
        res = registry.promote_model(version)
        model_service.load_model()
        return res
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/models/{version}/rollback")
def rollback_model_version(version: str):
    try:
        res = registry.rollback_model(version)
        model_service.load_model()
        return res
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))
