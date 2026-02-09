@echo off
echo ================================================
echo Receipt Warranty Tracker - Firebase Setup
echo ================================================
echo.

REM Step 1: Check for Firebase CLI
echo Step 1: Checking Firebase CLI installation...
where firebase >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Firebase CLI not found. Installing via npm...
    npm install -g firebase-tools
) else (
    echo Firebase CLI is installed.
)
echo.

REM Step 2: Check for Google Cloud CLI
echo Step 2: Checking Google Cloud CLI installation...
where gcloud >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Google Cloud CLI not found. Please install from:
    echo https://cloud.google.com/sdk/docs/install
    echo.
    echo After installing, run: gcloud init
) else (
    echo Google Cloud CLI is installed.
)
echo.

REM Step 3: Firebase Login
echo Step 3: Logging into Firebase...
echo Please log in with your Google account.
firebase login
echo.

REM Step 4: List Firebase projects
echo Step 4: Available Firebase projects:
firebase projects:list
echo.
echo Please create a new Firebase project at:
echo https://console.firebase.google.com
echo.

REM Step 5: Initialize Firebase in project
echo Step 5: Initialize Firebase in this project?
echo firebase init
echo.

REM Step 6: Get SHA-1 fingerprint
echo Step 6: Getting SHA-1 fingerprint...
echo Run this command to get your debug SHA-1:
echo.
echo keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
echo.
echo Add this SHA-1 to your Firebase project at:
echo https://console.firebase.google.com > project settings > Your apps > Android app
echo.

REM Step 7: Enable APIs
echo Step 7: Enable required APIs:
echo.
echo Run these commands to enable APIs:
echo.
echo gcloud services enable firestore.googleapis.com
echo gcloud services enable cloudfunctions.googleapis.com
echo gcloud services enable drive.googleapis.com
echo gcloud services enable firebase.googleapis.com
echo.

REM Step 8: Deploy Cloud Functions
echo Step 8: Deploy Cloud Functions
echo cd functions
echo pip install -r requirements.txt
echo firebase deploy --only functions
echo.

echo ================================================
echo Setup Complete!
echo ================================================
echo.
echo Next steps:
echo 1. Download google-services.json from Firebase Console
echo 2. Replace app/google-services.json with your file
echo 3. Run: cd functions ^&^& pip install -r requirements.txt
echo 4. Run: firebase deploy --only functions
echo 5. Open Android Studio and run the app
echo.
pause
