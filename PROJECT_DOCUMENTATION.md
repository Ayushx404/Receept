# Receipt Warranty Tracker - Complete Project Documentation

**Created:** February 8, 2026  
**Status:** Planning Complete - Ready for Implementation

---

## Table of Contents

1. [Original App Overview](#1-original-app-overview)
2. [Problems Identified](#2-problems-identified)
3. [Fixes Implemented](#3-fixes-implemented)
4. [New Dynamic App Requirements](#4-new-dynamic-app-requirements)
5. [Final Architecture Design](#5-final-architecture-design)
6. [Implementation Plan](#6-implementation-plan)
7. [Week-by-Week Breakdown](#7-week-by-week-breakdown)
8. [Technical Specifications](#8-technical-specifications)
9. [Cloud Functions (Python)](#9-cloud-functions-python)
10. [Documentation](#10-documentation)

---

## 1. Original App Overview

### App Name and Purpose
**Receipt Warranty Tracker** - An Android application for managing receipts and warranty cards with expiry reminders.

### Original Tech Stack
| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM with Repository Pattern |
| Database | Room (SQLite) |
| Min SDK | 34 (Android 14) |
| Target SDK | 35 |

---

## 2. Problems Identified

### Critical Issues
1. **Database Migration Crash** - SQLite ALTER TABLE failures
2. **Navigation Stack Issues** - Back stack corruption
3. **Edit/Detail Screen Item Not Found** - Filtered list issues
4. **Notification Worker Permission Crash** - Missing Android 14+ permission
5. **ViewModel Type Safety** - Unsafe casts
6. **Dashboard Nested NavigationBar** - Redundant navigation

### Major Issues
7. Type Parameter Validation
8. DetailScreen Null Handling
9. AddEditScreen Category Not Connected
10. Scheduler/Worker Initialization

---

## 3. Fixes Implemented

### All Critical Issues Resolved ✅

| Issue | Fix Applied |
|-------|-------------|
| Database Migration | Nuclear migration strategy with fallback |
| Navigation | Safe popUpTo with launchSingleTop |
| Item Fetching | Direct repository fetch instead of filtered list |
| Notifications | Permission check + unique IDs |
| ViewModel | Type-safe StateFlows |
| Dashboard | Removed nested NavigationBar |
| Detail Screen | Loading indicator for null items |
| Categories | Connected to ViewModel |
| WorkManager | Lazy initialization |
| Duplicate FAB | Removed from HomeScreen |

---

## 4. New Dynamic App Requirements

### Project Decisions

| Question | Answer |
|----------|--------|
| Cloud sync | ✅ Yes |
| Cross-device sync | ✅ Yes |
| Backend experience | Beginner (Python) |
| Timeline | 1 month |
| Web Dashboard | ❌ No |
| Push Notifications | ✅ Yes |
| Sync Notifications | ✅ Yes |
| User Categories | ✅ Yes |
| Customizable Theme | ✅ Yes |
| Drive Export | ✅ Yes |
| Offline Support | ✅ Yes |

---

## 5. Final Architecture Design

### Architecture Diagram
┌─────────────────────────────────────────────────────────┐ │ FRONTEND (Android) │ │ ┌─────────────────────────────────────────────────┐ │ │ │ Compose UI → ViewModels → Repository → Data │ │ │ └─────────────────────────────────────────────────┘ │ └─────────────────────────────────────────────────────────┘ │ │ HTTPS REST API ▼ ┌─────────────────────────────────────────────────────────┐ │ BACKEND (Python Firebase) │ │ ┌─────────────────────────────────────────────────┐ │ │ │ Cloud Functions (Python) │ │ │ │ ├── Auth → Sync → Export → Notifications │ │ │ └─────────────────────────────────────────────────┘ │ │ ┌─────────────────────────────────────────────────┐ │ │ │ Firebase: Firestore, Auth, FCM, Config │ │ │ └─────────────────────────────────────────────────┘ │ └─────────────────────────────────────────────────────────┘ │ │ Google APIs ▼ ┌─────────────────────────────────────────────────────────┐ │ EXTERNAL SERVICES │ │ Google Drive API | Firebase Cloud Messaging │ └─────────────────────────────────────────────────────────┘


---

## 6. Implementation Plan

### Data Models

**User Profile:**
```kotlin
data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val primaryColor: String = "#079992",
    val secondaryColor: String = "#047A74"
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }
Category:

data class Category(
    val id: String,
    val name: String,
    val icon: String = "category",
    val color: String = "#079992",
    val userId: String,
    val isDefault: Boolean = false
)
ReceiptWarranty:

data class ReceiptWarranty(
    val id: String,
    val userId: String,
    val type: ReceiptType,
    val title: String,
    val company: String,
    val categoryId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class SyncStatus { SYNCED, PENDING, CONFLICT }
7. Week-by-Week Breakdown
Week 1: Foundation (20 hours)
Day	Task	Hours
1	Firebase Project Setup	2
2	Android Studio Setup	2
3	Google Login	4
4	Room Database	3
5	User Profile System	3
6-7	Buffer/Review	6
Week 2: Core Features (25 hours)
Day	Task	Hours
1	Firestore Models	3
2	Sync Architecture	4
3	CRUD + Sync	5
4	HomeScreen Integration	4
5	Add/Edit Screens	5
6-7	Buffer/Testing	4
Week 3: Dynamic Features (25 hours)
Day	Task	Hours
1	Dynamic Categories	4
2	Theme System	4
3	Theme Application	4
4	Settings Screen	3
5	Dashboard Stats	3
6-7	Buffer/Polish	7
Week 4: Advanced Features (25 hours)
Day	Task	Hours
1	Drive API Setup	3
2	Export Function (Python)	4
3	Push Notifications	4
4	Sync Notifications UI	3
5	Testing	5
6	Documentation	4
7	Final Review	2
Total: 95 hours over 4 weeks
Week	Focus	Hours
1	Foundation	20
2	Core Features	25
3	Dynamic Features	25
4	Advanced Features	25
8. Technical Specifications
Dependencies
// Firebase BOM
implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")
implementation("com.google.firebase:firebase-functions")
implementation("com.google.firebase:firebase-config")
implementation("com.google.firebase:firebase-messaging")

// Google Sign-In
implementation("com.google.android.gms:play-services-auth:21.2.0")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.1.1")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.0")
File Structure
app/src/main/java/com/receiptwarranty/app/
├── MainActivity.kt
├── ReceiptWarrantyApp.kt
├── data/
│   ├── local/ (Room)
│   ├── remote/ (Firebase)
│   ├── repository/
│   └── sync/
├── domain/
│   ├── model/
│   └── usecase/
├── ui/
│   ├── navigation/
│   ├── screens/
│   ├── components/
│   └── theme/
└── workers/
9. Cloud Functions (Python)
Structure
functions/
├── main.py
├── requirements.txt
├── config.py
├── auth_handler.py
├── sync_handler.py
├── export_handler.py
└── notification_handler.py
Sample Function
@firestore_fn.on_document_created(
    document="users/{userId}/items/{itemId}",
    region="us-central1"
)
def on_item_created(event):
    user_id = event.params["userId"]
    db = firestore.client()
    db.collection("users").document(user_id).update({
        "lastSyncAt": firestore.SERVER_TIMESTAMP
    })
    send_sync_notification(user_id, "Item synced!")
10. Documentation Structure
# Receipt Warranty Tracker

## Description
Dynamic Android app with cloud sync, Google Login, and customizable themes.

## Features
- Google Authentication
- Cloud Sync (Offline-first)
- Dynamic Categories
- Customizable Themes
- Google Drive Export
- Real-time Dashboard
- Push Notifications

## Architecture
- Frontend: Android + Jetpack Compose
- Backend: Firebase (Firestore, Functions)
- Auth: Google OAuth 2.0
- Storage: Room (Offline) + Firestore (Cloud)

## Tech Stack
- Kotlin, Jetpack Compose
- Firebase (Auth, Firestore, Functions)
- Room Database
- WorkManager
- MVVM + Clean Architecture

## Setup Instructions
1. Configure Firebase
2. Enable Google Drive API
3. Add google-services.json
4. Run on emulator/device

## Demo Video
[Link to YouTube demo]

## Screenshots
[Add screenshots]
✅ All Fixes Complete
Original App Issues Resolved:
✅ Database Migration Fixed
✅ Navigation Fixed
✅ Item Fetching Fixed
✅ Notifications Fixed
✅ ViewModel Type Safety Fixed
✅ Dashboard Navigation Fixed
✅ DetailScreen Fixed
✅ AddEditScreen Categories Fixed
✅ WorkManager Init Fixed
✅ Duplicate FAB Removed
New Dynamic Features Ready for Implementation:
✅ Cloud Sync Architecture
✅ Google Login
✅ Dynamic Categories
✅ Customizable Themes (Colors + Day/Night)
✅ Google Drive Export
✅ Push Notifications
✅ Sync Notifications UI
✅ Offline-first Architecture
✅ Python Cloud Functions
✅ Firebase Integration
Project Status: ✅ Planning Complete
Ready for: Implementation Phase (4 weeks)
Estimated Total Time: 95 hours
Status: 🚀 Ready to Start!