# Receipt Warranty Tracker

A modern Android application for managing receipts and warranty cards with cloud sync, Google Login, and customizable themes.

## Features

- **Google Authentication** - Secure sign-in with Google account
- **Cloud Sync** - Offline-first architecture with real-time sync to Firebase
- **Dynamic Categories** - Create and manage custom categories
- **Customizable Themes** - Choose your preferred colors and theme mode (Light/Dark/System)
- **Google Drive Export** - Export your data as JSON to Google Drive
- **Warranty Reminders** - Get notified before warranties expire
- **Push Notifications** - Real-time sync and warranty expiry notifications

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Database**: Room (local) + Firestore (cloud)
- **Authentication**: Google OAuth 2.0
- **Background Sync**: WorkManager
- **Push Notifications**: Firebase Cloud Messaging
- **Backend**: Python Cloud Functions (Firebase)

## Setup Instructions

### 1. Firebase Setup

1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
2. Add an Android app with package name `com.receiptwarranty.app`
3. Enable Authentication → Google Sign-In provider
4. Enable Cloud Firestore Database
5. Enable Cloud Functions
6. Enable Firebase Cloud Messaging
7. Download `google-services.json` and replace the template in `app/`

### 2. Google Cloud Console Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Enable the following APIs:
   - Google Drive API
   - Firebase Cloud Messaging API
3. Create OAuth 2.0 credentials for Android
4. Add your app's SHA-1 fingerprint:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```

### 3. Cloud Functions Setup

```bash
cd functions
pip install -r requirements.txt
firebase deploy --only functions
```

### 4. Run the App

1. Open the project in Android Studio
2. Sync Gradle files
3. Run on an emulator or physical device

## Configuration

### Theme Colors

Default colors:
- Primary: #079992 (Teal)
- Secondary: #047A74 (Dark Teal)

### Sync Settings

- Sync interval: 15 minutes (configurable)
- Offline-first architecture: Changes are saved locally and synced when online

## Project Structure

```
app/src/main/java/com/receiptwarranty/app/
├── data/
│   ├── local/          # Room database, DAOs, DataStore
│   ├── remote/         # Firebase, Drive API
│   ├── repository/     # Repository pattern
│   └── sync/          # Sync manager
├── domain/
│   └── model/         # Domain models
├── ui/
│   ├── navigation/    # Navigation setup
│   ├── screens/       # UI screens
│   ├── components/   # Reusable components
│   └── theme/        # Theme configuration
└── workers/          # Background workers (FCM, reminders)

functions/           # Python Cloud Functions
├── main.py
├── auth_handler.py
├── sync_handler.py
├── notification_handler.py
└── export_handler.py
```

## API Reference

### Cloud Functions Endpoints

- `export_user_data?userId={uid}` - Export all user data
- `get_sync_status?userId={uid}` - Get sync status
- `update_profile` - Update user profile

### Firestore Collections

```
users/{userId}/
├── profile/          # User profile
├── receipts/        # Receipt documents
├── warranties/      # Warranty documents
├── categories/      # Category documents
├── syncMetadata/    # Sync metadata
└── fcmTokens/       # FCM tokens for notifications
```

## License

MIT License
