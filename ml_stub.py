from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import uvicorn

app = FastAPI()

class ComplaintData(BaseModel):
    text: str
    location: str = None
    category: str = None

class PredictionResponse(BaseModel):
    category: str
    priority: int
    estimated_resolution_time: int  # in hours

@app.post("/predict", response_model=PredictionResponse)
async def predict_complaint(data: ComplaintData):
    # Stub implementation - returns mock predictions
    return PredictionResponse(
        category="Municipal",
        priority=2,  # 1-3, where 1 is highest
        estimated_resolution_time=48  # hours
    )

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)