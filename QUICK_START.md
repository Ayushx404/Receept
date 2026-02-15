# Quick Start Guide - Receipt Warranty Tracker

## 5-Minute Setup

### Step 1: Download google-services.json
1. Go to: https://console.firebase.google.com/project/receipt-warranty-tracker
2. Click Settings (gear icon) > Project settings
3. Under "Your apps", click Android app
4. Download `google-services.json`
5. Replace: `app/google-services.json`

### Step 2: Get Your Web Client ID
1. Go to: https://console.cloud.google.com/apis/credentials
2. Find "OAuth 2.0 Client IDs"
3. Copy the Web client Client ID
4. Replace in `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="default_web_client_id">YOUR_ID.apps.googleusercontent.com</string>
   ```

### Step 3: Install Firebase CLI
```bash
npm install -g firebase-tools
firebase login
```

### Step 4: Initialize Firebase
```bash
firebase init
# Select: Firestore, Functions
# Select: Use existing project > receipt-warranty-tracker
# Language: Python
# Install dependencies: Yes
```

### Step 5: Enable Required APIs
```bash
gcloud services enable firestore.googleapis.com
gcloud services enable cloudfunctions.googleapis.com
gcloud services enable drive.googleapis.com
```

### Step 6: Deploy Functions
```bash
cd functions
pip install -r requirements.txt
firebase deploy --only functions
```

### Step 7: Run the App
1. Open Android Studio
2. Run the app

---

## Required Links

| Task | URL |
|------|-----|
| Firebase Console | https://console.firebase.google.com |
| Google Cloud Console | https://console.cloud.google.com |
| OAuth Credentials | https://console.cloud.google.com/apis/credentials |
| Enable Drive API | https://console.cloud.google.com/apis/library/drive.googleapis.com |
| Enable FCM API | https://console.cloud.google.com/apis/library/firebasecloudmessaging.googleapis.com |

## Common Commands

```bash
# Get SHA-1 fingerprint
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Deploy all Firebase services
firebase deploy

# Deploy only functions
firebase deploy --only functions

# Deploy only firestore rules
firebase deploy --only firestore

# View function logs
firebase functions:log

# Test functions locally
firebase emulators:start
```

## Project Files Created

```
PROJECT_ROOT/
├── firebase.json              # Firebase configuration
├── firestore.rules            # Firestore security rules
├── firestore.indexes.json     # Firestore indexes
├── .firebaserc               # Firebase projects mapping
├── functions/
│   ├── main.py               # Cloud Functions entry point
│   ├── requirements.txt      # Python dependencies
│   ├── auth_handler.py       # Auth functions
│   ├── sync_handler.py       # Sync functions
│   ├── notification_handler.py  # Push notifications
│   └── export_handler.py     # Export functions
├── app/
│   ├── google-services.json  # Firebase Android config (YOU NEED TO ADD THIS)
│   └── src/main/res/values/strings.xml  # Update web client ID
└── FIREBASE_SETUP_GUIDE.md   # Detailed setup guide
```

## Troubleshooting

| Error | Solution |
|-------|----------|
| "DEFAULT_WEB_CLIENT_ID not found" | Update strings.xml with your Web OAuth Client ID |
| "API not enabled" | Run gcloud services enable commands |
| "PERMISSION_DENIED" | Check Firestore rules allow authenticated users |
| "google-services.json not found" | Download from Firebase Console and add to app/ |

## Next Steps After Setup

1. Test Google Sign-In
2. Create a test receipt
3. Verify sync to Firestore
4. Test Drive export
5. Set up push notifications
