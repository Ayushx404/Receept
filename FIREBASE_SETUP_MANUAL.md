# Firebase Setup - Manual Steps

Since Firebase CLI requires browser login, please follow these steps:

## Step 1: Create Firebase Project (In Browser)

1. Go to: **https://console.firebase.google.com**
2. Click **"Add project"**
3. Enter project name: `receipt-warranty-tracker`
4. Disable Google Analytics (optional)
5. Wait for project creation

## Step 2: Add Android App

1. In Firebase Console, click **Android icon** (or "Add app" > "Android")
2. Enter:
   - **Android package name**: `com.receiptwarranty.app`
   - **App nickname**: `Receipt Warranty` (optional)
3. Click **"Register app"**
4. **IMPORTANT**: Download `google-services.json` and save it to:
   ```
   app/google-services.json
   ```
5. Click **"Next"** through remaining steps (skip SDK installation)

## Step 3: Enable Authentication

1. In Firebase Console, go to **Authentication** (left menu)
2. Click **"Get started"**
3. Click **"Sign-in method"** tab
4. Click on **Google** provider
5. Enable it (toggle to ON)
6. Click **"Save"**

## Step 4: Enable Firestore Database

1. In Firebase Console, go to **Firestore Database** (left menu)
2. Click **"Create database"**
3. Choose a location near you
4. Select **"Start in test mode"**
5. Click **"Enable"**

## Step 5: Enable Cloud Functions

1. In Firebase Console, go to **Functions** (left menu)
2. Click **"Get started"**
3. Accept to upgrade to **Blaze plan** (this is pay-as-you-go but free tier is generous)
4. Wait for initialization

## Step 6: Get Web Client ID (Important!)

1. Go to: **https://console.cloud.google.com/apis/credentials**
2. Select your project: `receipt-warranty-tracker`
3. Look for **"OAuth 2.0 Client IDs"** section
4. Find the **Web** client (not Android)
5. Copy the **Client ID** (ends in `.apps.googleusercontent.com`)
6. Open: `app/src/main/res/values/strings.xml`
7. Replace the placeholder:
   ```xml
   <string name="default_web_client_id">YOUR_COPIED_ID.apps.googleusercontent.com</string>
   ```

## Step 7: Enable Required APIs (Run in Command Prompt)

Open **Command Prompt** as Administrator and run:

```bash
gcloud services enable firestore.googleapis.com --project=receipt-warranty-tracker
gcloud services enable cloudfunctions.googleapis.com --project=receipt-warranty-tracker
gcloud services enable drive.googleapis.com --project=receipt-warranty-tracker
```

## Step 8: Initialize Firebase in Project

Open **Command Prompt** in your project folder (`C:\Users\aayus\Downloads\PROJECT`) and run:

```bash
cd C:\Users\aayus\Downloads\PROJECT

firebase login
firebase use receipt-warranty-tracker

firebase init
# Select these options:
# - Firestore: Configure
# - Functions: Configure
# - Use existing project: receipt-warranty-tracker
# - Language: Python
# - Install dependencies: Yes
```

## Step 9: Deploy Cloud Functions

```bash
cd C:\Users\aayus\Downloads\PROJECT\functions
pip install -r requirements.txt
firebase deploy --only functions
```

## Step 10: Run the App

1. Open **Android Studio**
2. Open the project
3. Wait for Gradle sync
4. Run on emulator or device

---

## Quick Links

| Task | URL |
|------|-----|
| Firebase Console | https://console.firebase.google.com |
| Google Cloud Console | https://console.cloud.google.com/apis/credentials |
| Enable Drive API | https://console.cloud.google.com/apis/library/drive.googleapis.com |

---

## If You Get Errors

**"DEFAULT_WEB_CLIENT_ID not found"**
- Make sure you updated `strings.xml` with your Web Client ID
- Client ID must be from Google Cloud Console, not Firebase Console

**"API not enabled"**
- Run the `gcloud services enable` commands from Step 7

**"PERMISSION_DENIED"**
- Make sure you're logged into Firebase CLI
- Make sure Authentication > Google is enabled

**"google-services.json not found"**
- Download it from Firebase Console > Project Settings > Your apps > Android app
- Save it to: `app/google-services.json`

---

## Files That Need Configuration

| File | What to Do |
|------|-----------|
| `app/google-services.json` | Download from Firebase Console |
| `app/src/main/res/values/strings.xml` | Replace `YOUR_WEB_CLIENT_ID` |
| `.firebaserc` | Will be created by `firebase use` command |
