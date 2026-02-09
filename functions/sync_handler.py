from firebase_admin import firestore
from firebase_functions import https_fn

def update_sync_metadata(user_id: str, action: str):
    """Update user's sync metadata"""
    db = firestore.client()
    db.collection("users").document(user_id).collection("syncMetadata").document("latest").set({
        "lastAction": action,
        "timestamp": firestore.SERVER_TIMESTAMP
    }, merge=True)

def get_last_sync_time(user_id: str) -> dict:
    """Get user's last sync time"""
    db = firestore.client()
    sync_ref = db.collection("users").document(user_id).collection("syncMetadata").document("latest").get()
    if sync_ref.exists:
        return sync_ref.to_dict()
    return {}

def cleanup_orphaned_documents(user_id: str):
    """Remove documents that exist in cloud but not in local"""
    db = firestore.client()
    
    receipts_ref = db.collection("users").document(user_id).collection("receipties")
    warranties_ref = db.collection("users").document(user_id).collection("warranties")
    categories_ref = db.collection("users").document(user_id).collection("categories")
    
    return {
        "receipts_count": len(receipts_ref.get()),
        "warranties_count": len(warranties_ref.get()),
        "categories_count": len(categories_ref.get())
    }

@https_fn.on_request(
    region="us-central1",
    memory=128,
    timeout_sec=30
)
def get_sync_status(request):
    """HTTP endpoint to get user's sync status"""
    user_id = request.args.get("userId")
    
    if not user_id:
        return {"error": "userId is required", "status": 400}
    
    try:
        sync_time = get_last_sync_time(user_id)
        return {
            "success": True,
            "syncMetadata": sync_time
        }
    except Exception as e:
        return {"error": str(e), "status": 500}
