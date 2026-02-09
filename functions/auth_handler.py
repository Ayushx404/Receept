from firebase_admin import firestore
from firebase_functions import https_fn

def validate_user(user_id: str) -> bool:
    """Validate if user exists in Firestore"""
    try:
        db = firestore.client()
        user_ref = db.collection("users").document(user_id).get()
        return user_ref.exists
    except Exception:
        return False

def get_user_profile(user_id: str) -> dict:
    """Get user profile from Firestore"""
    db = firestore.client()
    user_ref = db.collection("users").document(user_id).get()
    if user_ref.exists:
        return user_ref.to_dict()
    return {}

def create_user_profile(user_id: str, email: str, display_name: str) -> dict:
    """Create new user profile in Firestore"""
    db = firestore.client()
    user_data = {
        "uid": user_id,
        "email": email,
        "displayName": display_name,
        "createdAt": firestore.SERVER_TIMESTAMP,
        "themeMode": "SYSTEM",
        "primaryColor": "#079992",
        "secondaryColor": "#047A74"
    }
    db.collection("users").document(user_id).set(user_data)
    return user_data

def update_user_profile(user_id: str, updates: dict) -> bool:
    """Update user profile in Firestore"""
    try:
        db = firestore.client()
        db.collection("users").document(user_id).update(updates)
        return True
    except Exception:
        return False

@https_fn.on_request(
    region="us-central1",
    memory=128,
    timeout_sec=30
)
def update_profile(request):
    """HTTP endpoint to update user profile"""
    user_id = request.args.get("userId")
    data = request.get_json(silent=True)
    
    if not user_id:
        return {"error": "userId is required", "status": 400}
    
    if not validate_user(user_id):
        return {"error": "Unauthorized", "status": 401}
    
    try:
        update_user_profile(user_id, data)
        return {"success": True, "status": 200}
    except Exception as e:
        return {"error": str(e), "status": 500}
