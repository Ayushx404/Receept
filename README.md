# Receipt & Warranty (Android)

Android app for storing receipts/warranties with local Room storage, Google sign-in, Firestore sync, Google Drive image upload, and reminder notifications.

This README is generated from the current source code and project config in this repo.

## What is in this repo

- Android app module: `app/`
- Firebase config files: `firebase.json`, `.firebaserc`, `firestore.rules`, `firestore.indexes.json`
- No backend `functions/` directory exists in this repository

## Tech Stack (from code)

- Kotlin `2.0.0`
- Android Gradle Plugin `8.7.2`
- Gradle wrapper `8.9`
- Java target `17`
- Jetpack Compose (BOM `2024.06.00`)
- Room `2.6.1`
- WorkManager `2.9.0`
- Firebase Auth + Firestore (BOM `33.1.0`)
- Google Sign-In `21.2.0`
- Google Drive API (`google-api-client-android`, `google-api-services-drive`)
- ML Kit Text Recognition `16.0.0`

## Prerequisites

1. Android Studio (latest stable recommended)
2. Android SDK 35 installed
3. JDK 17
4. A Firebase project
5. A Google Cloud project (same project as Firebase) with Drive API enabled

## Install and Setup

### 1. Clone and open

```bash
git clone <your-repo-url>
cd PROJECT
```

Open the folder in Android Studio and let Gradle sync.

### 2. Configure Firebase Android app

In Firebase Console:

1. Create/select a project.
2. Add Android app with package name: `com.receiptwarranty.app`
3. Download `google-services.json`
4. Place it at: `app/google-services.json`

This repo currently already has an `app/google-services.json`; replace it if you are using your own Firebase project.

### 3. Configure Google Sign-In for Firebase Auth

The app uses `default_web_client_id` from `app/src/main/res/values/strings.xml`.

1. In Google Cloud Console, create OAuth credentials for your Firebase project.
2. Ensure you have a Web client ID.
3. Set this value in:
   - `app/src/main/res/values/strings.xml`
   - key: `default_web_client_id`

### 4. Add SHA fingerprints

Add your debug/release SHA certificates in Firebase Android app settings.

Debug SHA-1 example:

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

For Windows PowerShell, keystore is typically under:
`$env:USERPROFILE\.android\debug.keystore`

### 5. Enable required Firebase products

1. Firebase Authentication
   - Enable `Google` provider.
2. Cloud Firestore
   - Create database (native mode).

### 6. Enable required Google APIs

In Google Cloud Console, enable:

1. `Google Drive API`

Drive scopes are requested in code:
- `https://www.googleapis.com/auth/drive.file`
- `https://www.googleapis.com/auth/drive.appdata`

### 7. Deploy Firestore rules/indexes (optional but recommended)

If you use Firebase CLI:

```bash
npm install -g firebase-tools
firebase login
firebase use <your-firebase-project-id>
firebase deploy --only firestore
```

Rules and indexes are taken from:
- `firestore.rules`
- `firestore.indexes.json`

## Build and Run

### Android Studio

1. Sync Gradle.
2. Select a device/emulator with Android 14+ (project `minSdk = 34`).
3. Run app.

### Command line (Windows)

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## First-run behavior

1. App asks for:
   - Notifications (`POST_NOTIFICATIONS`, Android 13+)
   - Media images (`READ_MEDIA_IMAGES`)
2. User signs in with Google.
3. App initializes Firestore/Drive sync using authenticated user.

## Notes and Constraints from Code

- `minSdk = 34`, so Android 14+ devices/emulators are required.
- Local DB name: `receipt_warranty_db` (Room).
- WorkManager schedules warranty reminders.
- Firestore path pattern used by app:
  - `users/{uid}/receipts/*`
  - `users/{uid}/profile/info`
  - `users/{uid}/metadata/sync`

## Troubleshooting

1. `Sign in failed`
   - Recheck `default_web_client_id` and Firebase SHA fingerprints.
2. `No internet connection` during sync
   - Sync methods require network capability check to pass.
3. Drive upload/download not working
   - Confirm Drive API enabled and Google sign-in granted Drive scopes.
4. Build errors about Java/Kotlin
   - Ensure JDK 17 and Android Studio Gradle JDK is set to 17.
