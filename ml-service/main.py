from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from enum import Enum
from typing import Optional
import re

app = FastAPI(title="E-Mysore ML Service", version="1.0.0")

class Category(str, Enum):
    WATER = "WATER"
    STREET_LIGHT = "STREET_LIGHT"
    WASTE = "WASTE"
    ROADS = "ROADS"
    GENERAL = "GENERAL"

class Urgency(str, Enum):
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"

class Sentiment(str, Enum):
    POSITIVE = "POSITIVE"
    NEUTRAL = "NEUTRAL"
    NEGATIVE = "NEGATIVE"


class ComplaintInput(BaseModel):
    title: Optional[str] = None
    description: str

class TextInput(BaseModel):
    text: str

class Prediction(BaseModel):
    category: Category
    urgency: Urgency
    sentiment: Sentiment
    confidence: float


def analyze_text(text: str):
    """Helper function to analyze text using keyword-based heuristics"""
    text = text.lower()
    
    # Category detection
    if any(word in text for word in ["water", "leak", "pipe", "drinking"]):
        category = Category.WATER
    elif any(word in text for word in ["light", "street", "lamp", "dark"]):
        category = Category.STREET_LIGHT
    elif any(word in text for word in ["garbage", "waste", "trash", "clean"]):
        category = Category.WASTE
    elif any(word in text for word in ["road", "street", "pothole", "pavement"]):
        category = Category.ROADS
    else:
        category = Category.GENERAL

    # Urgency detection
    urgency_words = {
        "high": ["urgent", "immediately", "danger", "emergency", "critical"],
        "medium": ["soon", "needed", "required", "important"],
        "low": ["whenever", "sometime", "eventually"]
    }
    
    if any(word in text for word in urgency_words["high"]):
        urgency = Urgency.HIGH
    elif any(word in text for word in urgency_words["medium"]):
        urgency = Urgency.MEDIUM
    else:
        urgency = Urgency.LOW

    # Sentiment analysis
    negative_words = ["not happy", "bad", "poor", "danger", "unsafe", "terrible", "worst"]
    positive_words = ["good", "great", "excellent", "happy", "pleased", "thank"]
    
    if any(word in text for word in negative_words):
        sentiment = Sentiment.NEGATIVE
        confidence = 0.9
    elif any(word in text for word in positive_words):
        sentiment = Sentiment.POSITIVE
        confidence = 0.85
    else:
        sentiment = Sentiment.NEUTRAL
        confidence = 0.7

    return category, urgency, sentiment, confidence

@app.post("/predict", response_model=Prediction)
async def predict(input: ComplaintInput):
    """
    Analyze a complaint and return predictions for category, urgency, and sentiment.
    This is a rule-based stub that uses simple keyword matching. In production, this would use ML models.
    """
    text = " ".join(filter(None, [input.title, input.description]))
    if not text.strip():
        raise HTTPException(status_code=400, detail="Empty complaint text")

    category, urgency, sentiment, confidence = analyze_text(text)
    
    return Prediction(
        category=category,
        urgency=urgency,
        sentiment=sentiment,
        confidence=confidence
    )

@app.post("/sentiment")
async def analyze_sentiment(input: TextInput):
    """
    Analyze the sentiment of a text.
    This is a rule-based stub that uses simple keyword matching. In production, this would use a sentiment analysis model.
    """
    if not input.text.strip():
        raise HTTPException(status_code=400, detail="Empty text")

    _, _, sentiment, confidence = analyze_text(input.text)
    return {
        "sentiment": sentiment,
        "confidence": confidence
    }

@app.post("/category")
async def predict_category(input: TextInput):
    """
    Predict the category of a complaint from text.
    This is a rule-based stub that uses simple keyword matching. In production, this would use a classification model.
    """
    if not input.text.strip():
        raise HTTPException(status_code=400, detail="Empty text")

    category, _, _, confidence = analyze_text(input.text)
    return {
        "category": category,
        "confidence": confidence
    }

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "version": "1.0.0"}
