from firebase_admin import firestore
from firebase_functions import https_fn, messaging
import json

def send_sync_notification(user_id: str, message: str):
    """Send sync notification to user"""
    try:
        db = firestore.client()
        tokens_ref = db.collection("users").document(user_id).collection("fcmTokens")
        tokens = [doc.to_dict().get("token") for doc in tokens_ref.get() if doc.exists]
        
        if tokens:
            notification = messaging.Notification(
                title="Receipt Warranty Tracker",
                body=message
            )
            
            for token in tokens:
                messaging.send(messaging.Message(
                    notification=notification,
                    token=token
                ))
    except Exception as e:
        print(f"Error sending sync notification: {e}")

def send_warranty_expiry_notification(user_id: str, warranty_data: dict):
    """Send warranty expiry notification"""
    try:
        days_until_expiry = warranty_data.get('daysUntilExpiry', 0)
        if days_until_expiry <= 7 and days_until_expiry > 0:
            message = f"Your warranty for '{warranty_data.get('title', 'Unknown')}' expires in {days_until_expiry} day(s)"
            send_sync_notification(user_id, message)
    except Exception as e:
        print(f"Error sending warranty expiry notification: {e}")

@https_fn.on_call(
    region="us-central1",
    memory=128,
    timeout_sec=30
)
def register_fcm_token(request):
    """Register FCM token for user"""
    user_id = request.auth.get("uid") if request.auth else None
    token = request.data.get("token")
    
    if not user_id or not token:
        return {"error": "userId and token are required", "status": 400}
    
    try:
        db = firestore.client()
        db.collection("users").document(user_id).collection("fcmTokens").document(token).set({
            "token": token,
            "createdAt": firestore.SERVER_TIMESTAMP
        })
        return {"success": True}
    except Exception as e:
        return {"error": str(e), "status": 500}

@https_fn.on_call(
    region="us-central1",
    memory=128,
    timeout_sec=30
)
def unregister_fcm_token(request):
    """Unregister FCM token for user"""
    user_id = request.auth.get("uid") if request.auth else None
    token = request.data.get("token")
    
    if not user_id or not token:
        return {"error": "userId and token are required", "status": 400}
    
    try:
        db = firestore.client()
        db.collection("users").document(user_id).collection("fcmTokens").document(token).delete()
        return {"success": True}
    except Exception as e:
        return {"error": str(e), "status": 500}

@https_fn.on_request(
    region="us-central1",
    memory=128,
    timeout_sec=30
)
def send_test_notification(request):
    """Send test notification to user"""
    user_id = request.args.get("userId")
    
    if not user_id:
        return {"error": "userId is required", "status": 400}
    
    try:
        send_sync_notification(user_id, "This is a test notification!")
        return {"success": True, "message": "Notification sent"}
    except Exception as e:
        return {"error": str(e), "status": 500}
