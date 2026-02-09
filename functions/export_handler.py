from firebase_admin import firestore
from firebase_functions import https_fn
import json
from datetime import datetime

def generate_export(user_id: str) -> dict:
    """Generate export data for user"""
    db = firestore.client()
    
    user_ref = db.collection("users").document(user_id).get()
    user_data = user_ref.to_dict() if user_ref.exists else {}
    
    receipts = []
    for doc in db.collection("users").document(user_id).collection("receipts").get():
        receipts.append(doc.to_dict())
    
    warranties = []
    for doc in db.collection("users").document(user_id).collection("warranties").get():
        warranties.append(doc.to_dict())
    
    categories = []
    for doc in db.collection("users").document(user_id).collection("categories").get():
        categories.append(doc.to_dict())
    
    export_data = {
        "exportDate": datetime.now().isoformat(),
        "user": {
            "uid": user_data.get("uid"),
            "email": user_data.get("email"),
            "displayName": user_data.get("displayName")
        },
        "stats": {
            "totalReceipts": len(receipts),
            "totalWarranties": len(warranties),
            "totalCategories": len(categories)
        },
        "receipts": receipts,
        "warranties": warranties,
        "categories": categories
    }
    
    return export_data

@https_fn.on_request(
    region="us-central1",
    memory=256,
    timeout_sec=60
)
def export_all_data(request):
    """HTTP endpoint to export all user data as JSON"""
    user_id = request.args.get("userId")
    
    if not user_id:
        return {"error": "userId is required", "status": 400}
    
    try:
        export_data = generate_export(user_id)
        return https_fn.JSONResponse(export_data, status_code=200)
    except Exception as e:
        return {"error": str(e), "status": 500}

@https_fn.on_request(
    region="us-central1",
    memory=256,
    timeout_sec=60
)
def export_summary(request):
    """HTTP endpoint to export user summary"""
    user_id = request.args.get("userId")
    
    if not user_id:
        return {"error": "userId is required", "status": 400}
    
    try:
        db = firestore.client()
        
        receipts_count = len(db.collection("users").document(user_id).collection("receipts").get())
        warranties_count = len(db.collection("users").document(user_id).collection("warranties").get())
        categories_count = len(db.collection("users").document(user_id).collection("categories").get())
        
        summary = {
            "exportDate": datetime.now().isoformat(),
            "userId": user_id,
            "stats": {
                "totalReceipts": receipts_count,
                "totalWarranties": warranties_count,
                "totalCategories": categories_count
            }
        }
        
        return https_fn.JSONResponse(summary, status_code=200)
    except Exception as e:
        return {"error": str(e), "status": 500}
