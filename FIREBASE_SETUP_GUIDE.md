# Firebase Setup Guide for Receipt Warranty Tracker

This guide will help you set up Firebase and deploy the cloud functions.

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project"
3. Enter project name: `receipt-warranty-tracker`
4. Disable Google Analytics (optional, reduces setup time)
5. Wait for project creation to complete

## Step 2: Add Android App

1. In Firebase Console, click the Android icon to add an Android app
2. Enter package name: `com.receiptwarranty.app`
3. (Optional) Enter app nickname: `Receipt Warranty`
4. Click "Register app"
5. **Download google-services.json** and save it to: `app/google-services.json`
6. Click "Next" through remaining steps (skip Firebase SDK installation)

## Step 3: Enable Authentication

1. In Firebase Console, go to "Authentication" in left menu
2. Click "Get started"
3. Click "Sign-in method" tab
4. Click on "Google" provider
5. Enable it and click "Save"
6. Ensure email is listed in the "Email addresses" section

## Step 4: Enable Cloud Firestore

1. In Firebase Console, go to "Firestore Database" in left menu
2. Click "Create database"
3. Choose location closest to your users
4. Select "Start in test mode" for development (can switch to production later)
5. Click "Enable"

## Step 5: Enable Cloud Functions

1. In Firebase Console, go to "Functions" in left menu
2. Click "Get started"
3. Accept the defaults to upgrade to Blaze plan (free tier available)
4. Wait for initialization to complete

## Step 6: Enable Firebase Cloud Messaging

1. In Firebase Console, go to "Cloud Messaging" in left menu
2. Click "Get started"
3. The service should be automatically enabled

## Step 7: Configure Google Cloud Console

### Open Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Select your project: `receipt-warranty-tracker`

### Enable Required APIs

1. Go to "APIs & Services" > "Library"
2. Search for and enable these APIs:
   - **Google Drive API** - Required for Drive export feature
   - **Firebase Cloud Messaging API** - For push notifications

### Configure OAuth Consent Screen

1. Go to "APIs & Services" > "OAuth consent screen"
2. Choose "External" user type
3. Fill in:
   - App name: `Receipt Warranty Tracker`
   - User support email: your email
   - Email scopes (add email, profile)
4. Click "Save and continue"
5. Add test users (your email) for development
6. Click "Save and continue"

### Create OAuth 2.0 Credentials

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth client ID"
3. Application type: "Android"
4. Name: `Android Client`
5. Add your SHA-1 fingerprint:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
6. Add package name: `com.receiptwarranty.app`
7. Click "Create"
8. Note the "Client ID" - you'll need it for google-services.json

## Step 8: Get Your Web Client ID

1. In Google Cloud Console, go to "APIs & Services" > "Credentials"
2. Look for "OAuth 2.0 Client IDs" section
3. Find the "Web client" (not Android client)
4. Copy the Client ID (looks like: `xxx.apps.googleusercontent.com`)
5. Open `app/src/main/res/values/strings.xml` and update:
   ```xml
   <string name="default_web_client_id">YOUR_WEB_CLIENT_ID</string>
   ```

## Step 9: Set Up Firebase CLI

1. Install Node.js from [nodejs.org](https://nodejs.org)
2. Open terminal/command prompt
3. Install Firebase CLI:
   ```bash
   npm install -g firebase-tools
   ```
4. Log in to Firebase:
   ```bash
   firebase login
   ```
5. Initialize Firebase in your project:
   ```bash
   firebase init
   ```
   Select:
   - Firestore: Configure
   - Functions: Configure
   - Use existing project: Select `receipt-warranty-tracker`
   - Language: Python
   - Install dependencies: Yes

## Step 10: Deploy Cloud Functions

1. Install Python dependencies:
   ```bash
   cd functions
   pip install -r requirements.txt
   ```
2. Deploy functions:
   ```bash
   firebase deploy --only functions
   ```

## Step 11: Run the App

1. Open Android Studio
2. Open the project
3. Wait for Gradle sync to complete
4. Run on emulator or device (API 34+)

## Troubleshooting

### "Default web client ID not found"

1. Make sure you created OAuth credentials in Google Cloud Console
2. Ensure the Web OAuth client exists (not just Android)
3. Check that strings.xml has the correct Client ID

### "API not enabled" errors

1. Enable APIs in Google Cloud Console:
   ```bash
   gcloud services enable firestore.googleapis.com
   gcloud services enable cloudfunctions.googleapis.com
   gcloud services enable drive.googleapis.com
   ```

### "No Firebase App has been created"

1. Ensure google-services.json is in `app/` directory
2. Check package name matches exactly: `com.receiptwarranty.app`

### Functions deployment fails

1. Make sure you're on Firebase Blaze plan (pay-as-you-go)
2. Check functions/logs in Firebase Console for errors
3. Ensure Python syntax is correct in functions/

## Firebase Console URLs

- Firebase Console: https://console.firebase.google.com
- Google Cloud Console: https://console.cloud.google.com
- OAuth Consent Screen: https://console.cloud.google.com/apis/credentials/consent
- OAuth Credentials: https://console.cloud.google.com/apis/credentials
