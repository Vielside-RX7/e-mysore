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
    assigned_dept: str | None = None
    department_hierarchy: list | None = None
    urgency: str | None = None
    sentiment: str | None = None
    confidence: float | None = None

@app.post("/predict", response_model=PredictionResponse)
async def predict_complaint(data: ComplaintData):
    # Improved rule-based classifier with simple scoring and confidence
    text = (data.text or "").lower()
    category = (data.category or "").lower()

    # Keywords and weights for departments
    dept_keywords = {
        "MCC – Engineering (Roads) Section": {
            "keywords": ["road", "pothole", "speedbreaker", "asphalt", "intersection"],
            "hierarchy": ["Assistant Engineer (AE)", "Junior Engineer (JE)", "MCC Executive Engineer (EE)", "MCC Commissioner", "PWD"]
        },
        "Vani Vilas Water Works (VVWW) – Pipeline Section": {
            "keywords": ["water", "pipe", "leak", "water supply", "tap", "burst"],
            "hierarchy": ["JE / AE Water Supply", "MCC Water Engineer", "KUWS&DB", "MCC Commissioner"]
        },
        "MCC – SWM Ward Supervisor": {
            "keywords": ["garbage", "waste", "dump", "bin", "landfill", "swm", "trash"],
            "hierarchy": ["Health Inspector (HI)", "MCC Zonal Health Officer", "MCC Health Officer", "MCC Commissioner"]
        },
        "CESC – Section Office": {
            "keywords": ["electric", "power", "cable", "line", "transformer", "live wire"],
            "hierarchy": ["Lineman / Junior Engineer (JE)", "CESC AEE", "CESC EE", "CESC Superintendent Engineer (SE)"]
        },
        "MCC Electrical Section – JE / AE": {
            "keywords": ["streetlight", "light", "lamp post", "street light"],
            "hierarchy": ["MCC Electrical Executive Engineer", "MCC Commissioner"]
        },
        "MCC – UGD Section": {
            "keywords": ["drain", "sewer", "storm", "manhole", "ugd", "sewage"],
            "hierarchy": ["JE / AE (Drainage)", "MCC Executive Engineer (UGD)", "KUWS&DB", "MCC Commissioner"]
        },
        "Local Police Station": {
            "keywords": ["police", "crime", "robbery", "assault", "theft", "safety"],
            "hierarchy": ["Sub-Inspector (SI)", "Circle Inspector (CI)", "ACP", "DCP", "Commissioner of Police"]
        },
        "Fire Station": {
            "keywords": ["fire", "burn", "accident", "smoke", "sparks"],
            "hierarchy": ["Fire Station Officer", "Fire Service District Command Office"]
        }
    }

    # Scoring: count weighted keyword matches
    scores = {}
    total_possible = 0
    for dept, info in dept_keywords.items():
        kws = info["keywords"]
        total_possible += len(kws)
        score = 0
        for kw in kws:
            if kw in text:
                # more specific phrases get higher weight
                weight = 1.5 if ' ' in kw else 1.0
                score += weight
        # slight boost if category matches dept hints
        if dept.lower().split()[0] in category:
            score += 0.5
        scores[dept] = score

    # Choose best dept
    best_dept = None
    best_score = 0.0
    for dept, s in scores.items():
        if s > best_score:
            best_score = s
            best_dept = dept

    # If no matches, fallback to ward office
    if best_dept is None or best_score == 0.0:
        best_dept = "Ward Office / Local MCC Office"
        best_hierarchy = ["Concerned Department JE/AE", "Department Executive Engineer", "MCC Commissioner", "Deputy Commissioner"]
        confidence = 0.3
    else:
        best_hierarchy = dept_keywords[best_dept]["hierarchy"]
        # confidence derived from best_score relative to total keywords
        confidence = min(0.99, best_score / max(1.0, total_possible / 4.0))

    # Derive urgency from keywords / priority
    urgency = "MEDIUM"
    if any(w in text for w in ["accident", "injury", "life", "danger", "collapse"]):
        urgency = "HIGH"
    elif any(w in text for w in ["leak", "overflow", "pothole", "broken"]):
        urgency = "MEDIUM"
    else:
        urgency = "LOW"

    # Basic sentiment polarity
    sentiment = "NEUTRAL"
    if any(w in text for w in ["angry", "furious", "outraged", "unacceptable", "terrible"]):
        sentiment = "NEGATIVE"
    elif any(w in text for w in ["thank", "thanks", "appreciate", "grateful"]):
        sentiment = "POSITIVE"

    # map urgency to an estimated resolution time (hours) and numeric priority
    est_hours = 72
    priority = 2
    if urgency == "HIGH":
        est_hours = 24
        priority = 1
    elif urgency == "MEDIUM":
        est_hours = 48
        priority = 2
    else:
        est_hours = 120
        priority = 3

    # Build response
    return PredictionResponse(
        category=(data.category or "GENERAL").upper(),
        priority=priority,
        estimated_resolution_time=est_hours,
        assigned_dept=best_dept,
        department_hierarchy=best_hierarchy,
        urgency=urgency,
        sentiment=sentiment,
        confidence=round(float(confidence), 2)
    )

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)