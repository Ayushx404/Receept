#!/bin/bash

echo "================================================"
echo "Receipt Warranty Tracker - Firebase Setup"
echo "================================================"

# Step 1: Check for Firebase CLI
echo ""
echo "Step 1: Checking Firebase CLI installation..."
if ! command -v firebase &> /dev/null; then
    echo "Firebase CLI not found. Installing via npm..."
    npm install -g firebase-tools
else
    echo "Firebase CLI is installed."
fi

# Step 2: Check for Google Cloud CLI
echo ""
echo "Step 2: Checking Google Cloud CLI installation..."
if ! command -v gcloud &> /dev/null; then
    echo "Google Cloud CLI not found. Please install from:"
    echo "https://cloud.google.com/sdk/docs/install"
    echo ""
    echo "After installing, run: gcloud init"
else
    echo "Google Cloud CLI is installed."
fi

# Step 3: Firebase Login
echo ""
echo "Step 3: Logging into Firebase..."
echo "Please log in with your Google account."
firebase login

# Step 4: Initialize Firebase
echo ""
echo "Step 4: Initialize Firebase in this project..."
firebase init

# Step 5: Get SHA-1 fingerprint
echo ""
echo "Step 5: Getting SHA-1 fingerprint..."
echo "Run this command to get your debug SHA-1:"
echo ""
echo "keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android"
echo ""
echo "Add this SHA-1 to your Firebase project at:"
echo "https://console.firebase.google.com > Project Settings > Your apps > Android app"

# Step 6: Enable APIs
echo ""
echo "Step 6: Enable required APIs..."
echo ""
echo "Running these commands:"
gcloud services enable firestore.googleapis.com
gcloud services enable cloudfunctions.googleapis.com
gcloud services enable drive.googleapis.com
gcloud services enable firebase.googleapis.com

# Step 7: Install Python dependencies
echo ""
echo "Step 7: Installing Python dependencies..."
cd functions
pip install -r requirements.txt

# Step 8: Deploy Cloud Functions
echo ""
echo "Step 8: Deploy Cloud Functions..."
firebase deploy --only functions

echo ""
echo "================================================"
echo "Setup Complete!"
echo "================================================"
echo ""
echo "Next steps:"
echo "1. Download google-services.json from Firebase Console"
echo "2. Replace app/google-services.json with your file"
echo "3. Open Android Studio and run the app"
echo ""
