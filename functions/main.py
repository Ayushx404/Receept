import os
from firebase_functions import firestore_fn, https_fn, identity_fn
from functions.auth_handler import validate_user, get_user_profile
from functions.sync_handler import update_sync_metadata
from functions.notification_handler import send_sync_notification, send_warranty_expiry_notification
from functions.export_handler import generate_export

@firestore_fn.on_document_created(
    document="users/{userId}/receipts/{receiptId}",
    region="us-central1",
    memory=256,
    timeout_sec=30
)
def on_receipt_created(event):
    """Triggered when a receipt is created in Firestore"""
    user_id = event.params["userId"]
    receipt_data = event.data.to_dict()
    
    update_sync_metadata(user_id, "receipt_created")
    
    send_sync_notification(
        user_id,
        f"Receipt '{receipt_data.get('title', 'Unknown')}' synced to cloud!"
    )

@firestore_fn.on_document_created(
    document="users/{userId}/warranties/{warrantyId}",
    region="us-central1",
    memory=256,
    timeout_sec=30
)
def on_warranty_created(event):
    """Triggered when a warranty is created in Firestore"""
    user_id = event.params["userId"]
    warranty_data = event.data.to_dict()
    
    update_sync_metadata(user_id, "warranty_created")
    
    send_sync_notification(
        user_id,
        f"Warranty '{warranty_data.get('title', 'Unknown')}' synced to cloud!"
    )

@firestore_fn.on_document_updated(
    document="users/{userId}/warranties/{warrantyId}",
    region="us-central1",
    memory=256,
    timeout_sec=30
)
def on_warranty_updated(event):
    """Triggered when a warranty is updated"""
    user_id = event.params["userId"]
    warranty_data = event.data.to_dict()
    
    update_sync_metadata(user_id, "warranty_updated")
    
    expiry_date = warranty_data.get('expiryDate')
    if expiry_date:
        send_warranty_expiry_notification(user_id, warranty_data)

@firestore_fn.on_document_deleted(
    document="users/{userId}/receipts/{receiptId}",
    region="us-central1"
)
def on_receipt_deleted(event):
    """Triggered when a receipt is deleted"""
    user_id = event.params["userId"]
    update_sync_metadata(user_id, "receipt_deleted")

@firestore_fn.on_document_deleted(
    document="users/{userId}/warranties/{warrantyId}",
    region="us-central1"
)
def on_warranty_deleted(event):
    """Triggered when a warranty is deleted"""
    user_id = event.params["userId"]
    update_sync_metadata(user_id, "warranty_deleted")

@https_fn.on_request(
    region="us-central1",
    memory=256,
    timeout_sec=60
)
def export_user_data(request):
    """HTTP endpoint to export all user data"""
    user_id = request.args.get("userId")
    
    if not user_id:
        return https_fn.JSONResponse({"error": "userId is required"}, status_code=400)
    
    if not validate_user(user_id):
        return https_fn.JSONResponse({"error": "Unauthorized"}, status_code=401)
    
    try:
        export_data = generate_export(user_id)
        return https_fn.JSONResponse({
            "success": True,
            "data": export_data
        }, status_code=200)
    except Exception as e:
        return https_fn.JSONResponse({
            "error": str(e)
        }, status_code=500)

@https_fn.on_call(
    region="us-central1",
    memory=128,
    timeout_sec=30
)
def get_user_stats(request):
    """Callable function to get user statistics"""
    user_id = request.auth.get("uid") if request.auth else None
    
    if not user_id:
        return {"error": "Unauthorized", "status": 401}
    
    try:
        profile = get_user_profile(user_id)
        return {
            "success": True,
            "profile": profile
        }
    except Exception as e:
        return {"error": str(e), "status": 500}

@firestore_fn.on_document_created(
    document="users/{userId}/categories/{categoryId}",
    region="us-central1"
)
def on_category_created(event):
    """Triggered when a category is created"""
    user_id = event.params["userId"]
    update_sync_metadata(user_id, "category_created")
