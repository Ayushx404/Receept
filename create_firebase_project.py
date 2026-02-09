#!/usr/bin/env python3
"""
Firebase Project Setup Script for Receipt Warranty Tracker
This script will help you create a Firebase project and configure everything.
"""

import os
import subprocess
import json

def run_command(cmd, description):
    print(f"\n{description}...")
    print(f"Command: {cmd}")
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"Error: {result.stderr}")
        return False
    print(f"Success: {result.stdout.strip()}")
    return True

def main():
    print("=" * 60)
    print("Receipt Warranty Tracker - Firebase Project Setup")
    print("=" * 60)

    # Step 1: Check Firebase login
    print("\nStep 1: Checking Firebase login...")
    result = subprocess.run("firebase whoami", shell=True, capture_output=True, text=True)
    if result.returncode != 0:
        print("Please log in to Firebase first:")
        print("  firebase login")
        return
    print(f"Logged in as: {result.stdout.strip()}")

    # Step 2: Create project
    print("\nStep 2: Creating Firebase project...")
    project_id = "receipt-warranty-tracker"
    display_name = "Receipt Warranty Tracker"

    response = input(f"Create new project '{project_id}'? (y/n): ")
    if response.lower() == 'y':
        cmd = f'firebase projects:create {project_id} --display-name "{display_name}"'
        if not run_command(cmd, "Creating Firebase project"):
            print("Failed to create project. You may need to create it manually at:")
            print("  https://console.firebase.google.com")
            return
    else:
        print("Skipping project creation. Make sure you have a Firebase project ready.")
        project_id = input("Enter your existing project ID: ").strip()

    # Step 3: Use the project
    print("\nStep 3: Configuring Firebase to use this project...")
    cmd = f"firebase use {project_id}"
    if not run_command(cmd, "Setting active Firebase project"):
        print("Failed to set project. Run manually: firebase use <project-id>")

    # Step 4: Update .firebaserc
    print("\nStep 4: Updating .firebaserc...")
    with open('.firebaserc', 'w') as f:
        json.dump({
            "projects": {
                "default": project_id
            }
        }, f, indent=2)
    print(f"Updated .firebaserc with project: {project_id}")

    # Step 5: Print next steps
    print("\n" + "=" * 60)
    print("NEXT STEPS - Do these in your browser:")
    print("=" * 60)
    print(f"""
1. Go to Firebase Console:
   https://console.firebase.google.com/project/{project_id}

2. Add Android App:
   - Click Android icon
   - Package name: com.receiptwarranty.app
   - Download google-services.json
   - Save to: app/google-services.json

3. Enable Authentication:
   - Go to Authentication > Sign-in method
   - Enable "Google" provider

4. Enable Firestore:
   - Go to Firestore Database
   - Click "Create database"
   - Choose location
   - Select "Start in test mode"

5. Enable Cloud Functions:
   - Go to Functions
   - Click "Get started"
   - Accept to upgrade to Blaze plan

6. Get Web Client ID:
   - Go to Google Cloud Console:
     https://console.cloud.google.com/apis/credentials
   - Find "OAuth 2.0 Client IDs"
   - Copy the Web client ID
   - Update: app/src/main/res/values/strings.xml
""")

    print("\n7. Enable APIs (run these commands):")
    print(f"   gcloud services enable firestore.googleapis.com --project={project_id}")
    print(f"   gcloud services enable cloudfunctions.googleapis.com --project={project_id}")
    print(f"   gcloud services enable drive.googleapis.com --project={project_id}")

    print("\n8. Deploy Functions:")
    print("   cd functions")
    print("   pip install -r requirements.txt")
    print("   firebase deploy --only functions")

    print("\n" + "=" * 60)
    print("Setup Guide: See QUICK_START.md")
    print("=" * 60)

if __name__ == "__main__":
    main()
