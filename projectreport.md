# PROJECT REPORT

## CHAPTER 1: INTRODUCTION

### 1.1 Background
Receipt and warranty documents are commonly stored in paper form or scattered across gallery apps, email threads, and cloud folders. This makes it difficult to find proof of purchase quickly, track warranty expiration, and maintain records across devices.

The current project, **Receipt & Warranty**, is an Android application built with Kotlin and Jetpack Compose. It combines local storage (Room) with cloud sync (Firebase Firestore) and Google Sign-In, so users can maintain a reliable personal archive of receipts and warranties.

### 1.2 Objective
1. Build a mobile-first system for storing and managing receipts and warranties.
2. Provide secure user authentication through Google Sign-In.
3. Support offline-first usage with local database persistence.
4. Synchronize user data with cloud storage for cross-device continuity.
5. Send reminder notifications before warranty expiry.
6. Support structured search, filtering, and category-based organization.

### 1.3 Purpose and Scope

#### 1.3.1 Purpose
The purpose of the system is to reduce loss of purchase records, improve warranty tracking, and provide a single trusted place for users to manage post-purchase documentation.

#### 1.3.2 Scope
The current implementation scope includes:
1. Android application (`minSdk 34`, `targetSdk 35`).
2. Google authentication and user session handling.
3. Receipt/Warranty CRUD operations with images and notes.
4. Local storage using Room database.
5. Cloud sync with Firestore and image handling via Google Drive API.
6. Reminder scheduling using WorkManager.
7. CSV export and dashboard statistics.

Out-of-scope for this repository:
1. Dedicated custom backend service (no separate backend server code in this repo).
2. Web portal/admin panel.

---

## CHAPTER 2: SURVEY OF TECHNOLOGY

### 2.1 Justification of Selection of Technology
The selected stack prioritizes Android-native performance, maintainability, and cloud integration speed.

#### 2.1.1 Front End
1. **Kotlin**: modern, concise, null-safe language for Android.
2. **Jetpack Compose**: declarative UI with faster iteration and cleaner state handling.
3. **Material 3**: consistent, modern UI components.
4. **Navigation Compose**: route-driven screen transitions.

#### 2.1.2 Back End
1. **Firebase Authentication**: secure sign-in with minimal backend setup.
2. **Cloud Firestore**: scalable document database for user-scoped data.
3. **Google Drive API**: cloud image storage workflow tied to user account.
4. **BaaS model**: backend capabilities without maintaining custom server infrastructure.

---

## CHAPTER 3: REQUIREMENT AND ANALYSIS

### 3.1 Existing System
In common manual or fragmented digital approaches:
1. Receipts are stored physically and often lost/faded.
2. Warranty expiry is tracked manually or not tracked at all.
3. No unified searchable catalog exists.
4. Cross-device continuity is poor.

### 3.2 Proposed System
The proposed system is an Android app with:
1. Structured receipt/warranty records.
2. Local-first persistence using Room.
3. Cloud sync through Firestore.
4. Google account-based access and personalization.
5. Reminder notifications and dashboard insights.

### 3.3 Requirement Analysis

#### Functional Requirements
```mermaid
flowchart TD
    FR[Functional Requirements]
    FR --> FR1["FR-1: Sign in/out using Google account"]
    FR --> FR2["FR-2: Add, edit, view, and delete receipt/warranty items"]
    FR --> FR3["FR-3: Attach and view item images"]
    FR --> FR4["FR-4: Assign categories and notes"]
    FR --> FR5["FR-5: Filter/search items by type, status, and query"]
    FR --> FR6["FR-6: Sync local data with cloud"]
    FR --> FR7["FR-7: Receive warranty expiry reminders"]
    FR --> FR8["FR-8: Export data to CSV"]
```

#### Non-Functional Requirements
```mermaid
flowchart TD
    NFR[Non-Functional Requirements]
    NFR --> NFR1["NFR-1: Offline support for core CRUD operations"]
    NFR --> NFR2["NFR-2: Sync resilience to network fluctuations"]
    NFR --> NFR3["NFR-3: Responsive UI on mid-range devices"]
    NFR --> NFR4["NFR-4: User-scoped authenticated data access"]
    NFR --> NFR5["NFR-5: Modular and maintainable architecture"]
```

### 3.4 Planning and Scheduling
```mermaid
gantt
    title Receipt & Warranty Project Plan
    dateFormat  YYYY-MM-DD
    section Foundation
    Project Setup & Gradle Config       :done, a1, 2026-02-01, 2d
    Local DB (Room) + Models            :done, a2, after a1, 4d
    section Core Features
    UI Screens + Navigation             :done, b1, 2026-02-07, 6d
    CRUD + Validation                   :done, b2, after b1, 4d
    section Cloud & Sync
    Google Sign-In + Firebase Auth      :done, c1, 2026-02-12, 3d
    Firestore Sync                      :done, c2, after c1, 5d
    Drive Image Handling                :done, c3, after c2, 3d
    section Reliability
    Reminder Worker + Notifications     :done, d1, 2026-02-18, 3d
    Bug Fixes + Testing                 :active, d2, after d1, 6d
    section Documentation
    Final Report + README               :active, e1, 2026-02-24, 2d
```

### 3.5 Hardware Requirements

#### Development Machine
```mermaid
flowchart TD
    DEV[Development Machine Requirements]
    DEV --> DEV1["CPU: 64-bit quad-core"]
    DEV --> DEV2["RAM: 16 GB recommended"]
    DEV --> DEV3["Storage: 20 GB free disk space"]
    DEV --> DEV4["Network: Stable internet for Firebase/Gradle dependencies"]
```

#### Target Device
```mermaid
flowchart TD
    TGT[Target Device Requirements]
    TGT --> TGT1["OS: Android 14 (API 34) or above"]
    TGT --> TGT2["RAM: 4 GB recommended"]
    TGT --> TGT3["Storage: 500 MB free for app data/images"]
    TGT --> TGT4["Connectivity: Internet required for sync and sign-in"]
```

### 3.6 Software Requirements
```mermaid
flowchart TD
    SW[Software Requirements]
    SW --> SW1["OS (dev): Windows/macOS/Linux"]
    SW --> SW2["Android Studio: Latest stable"]
    SW --> SW3["JDK: 17"]
    SW --> SW4["Gradle Wrapper: 8.9"]
    SW --> SW5["Android Gradle Plugin: 8.7.2"]
    SW --> SW6["Kotlin: 2.0.0"]
    SW --> SW7["Firebase Project: Auth + Firestore enabled"]
    SW --> SW8["Google Cloud APIs: Drive API enabled"]
```

---

## CHAPTER 4: SYSTEM DESIGN

### 4.1 Tree View
```mermaid
graph TD
    A[Receipt & Warranty App] --> B[Presentation Layer]
    A --> C[Domain/Logic Layer]
    A --> D[Data Layer]
    A --> E[Cloud Services]

    B --> B1[Login Screen]
    B --> B2[Home Screen]
    B --> B3[Add/Edit Screen]
    B --> B4[Detail Screen]
    B --> B5[Dashboard Screen]
    B --> B6[Settings Screen]

    C --> C1[ReceiptWarrantyViewModel]
    C --> C2[AuthViewModel]
    C --> C3[SyncStateManager]

    D --> D1[Room Database]
    D --> D2[DAO]
    D --> D3[Repository]
    D --> D4[Workers]

    E --> E1[Firebase Auth]
    E --> E2[Firestore]
    E --> E3[Google Drive API]
```

### 4.2 Module Division
```mermaid
flowchart LR
    U[User] --> UI[Compose UI Module]
    UI --> VM[ViewModel Module]
    VM --> REPO[Repository Module]
    REPO --> ROOM[Room Local DB]
    REPO --> SYNC[Sync Manager]
    SYNC --> FS[Firestore Repository]
    SYNC --> DRIVE[Drive Storage Manager]
    VM --> WM[WorkManager Reminder Module]
    UI --> AUTH[Google Auth Module]
    AUTH --> FBA[Firebase Authentication]
```

### 4.3 Data Dictionary

#### Local Entity: `receipt_warranty`
```mermaid
flowchart TD
    RW["Entity: receipt_warranty"]
    RW --> RW1["id: Long (PK)<br/>Local unique ID"]
    RW --> RW2["type: Enum RECEIPT/WARRANTY<br/>Item type"]
    RW --> RW3["title: String<br/>Item title"]
    RW --> RW4["company: String<br/>Store/company name"]
    RW --> RW5["category: String?<br/>User category"]
    RW --> RW6["imageUri: String?<br/>Local/cloud image URI"]
    RW --> RW7["driveFileId: String?<br/>Google Drive file ID"]
    RW --> RW8["cloudId: String?<br/>Firestore document ID"]
    RW --> RW9["purchaseDate: Long?<br/>Purchase timestamp"]
    RW --> RW10["warrantyExpiryDate: Long?<br/>Expiry timestamp"]
    RW --> RW11["reminderDays: Enum?<br/>Reminder offset"]
    RW --> RW12["notes: String?<br/>Additional notes"]
    RW --> RW13["createdAt: Long<br/>Creation time"]
    RW --> RW14["updatedAt: Long<br/>Last update time"]
```

#### Local Entity: `deleted_items`
```mermaid
flowchart TD
    DI["Entity: deleted_items"]
    DI --> DI1["cloudId: String (PK-part)<br/>Cloud document ID of deleted item"]
    DI --> DI2["userId: String (PK-part)<br/>User identifier"]
    DI --> DI3["deletedAt: Long<br/>Deletion timestamp"]
```

#### Cloud Collections (Firestore)
```mermaid
graph TD
    U["users/{uid}"] --> R["receipts"]
    U --> P["profile"]
    U --> M["metadata"]

    R --- R1["Cloud receipt/warranty documents"]
    P --- P1["User profile document"]
    M --- M1["Sync metadata document"]
```


### 4.4 ER Diagrams
```mermaid
erDiagram
    USER ||--o{ RECEIPT_WARRANTY : owns
    USER ||--o{ DELETED_ITEM : tracks

    USER {
        string userId
        string email
        string displayName
    }

    RECEIPT_WARRANTY {
        long id PK
        string cloudId
        string type
        string title
        string company
        string category
        string imageUri
        string driveFileId
        long purchaseDate
        long warrantyExpiryDate
        string reminderDays
        string notes
        long createdAt
        long updatedAt
    }

    DELETED_ITEM {
        string cloudId PK
        string userId PK
        long deletedAt
    }
```


### 4.5 DFD/UML Diagrams

#### DFD (Level 0)
```mermaid
flowchart TD
    User[User] --> App[Receipt & Warranty App]
    App --> LocalDB[(Room Database)]
    App --> Auth[Firebase Auth]
    App --> CloudDB[(Firestore)]
    App --> Drive[(Google Drive)]
    App --> Notify[Notification Service]

    Auth --> App
    CloudDB --> App
    Drive --> App
    LocalDB --> App
```

#### UML Class Diagram
```mermaid
classDiagram
    class MainActivity
    class ReceiptWarrantyNavHost
    class AuthViewModel
    class ReceiptWarrantyViewModel
    class ReceiptWarrantyRepository
    class ReceiptWarrantyDao
    class AppDatabase
    class SyncStateManager
    class FirestoreRepository
    class DriveStorageManager
    class WarrantyReminderScheduler
    class WarrantyReminderWorker

    MainActivity --> AuthViewModel
    MainActivity --> ReceiptWarrantyNavHost
    ReceiptWarrantyNavHost --> ReceiptWarrantyViewModel
    ReceiptWarrantyViewModel --> ReceiptWarrantyRepository
    ReceiptWarrantyViewModel --> SyncStateManager
    ReceiptWarrantyViewModel --> WarrantyReminderScheduler
    ReceiptWarrantyRepository --> ReceiptWarrantyDao
    ReceiptWarrantyDao --> AppDatabase
    SyncStateManager --> FirestoreRepository
    SyncStateManager --> DriveStorageManager
    WarrantyReminderScheduler --> WarrantyReminderWorker
```

#### UML Sequence Diagram (Add Item + Sync)
```mermaid
sequenceDiagram
    participant U as User
    participant UI as AddEditScreen
    participant VM as ReceiptWarrantyViewModel
    participant Repo as ReceiptWarrantyRepository
    participant Sync as SyncStateManager
    participant FS as FirestoreRepository

    U->>UI: Enter item details and Save
    UI->>VM: onSave(item)
    VM->>Repo: insertOrUpdate(item)
    Repo-->>VM: local ID
    VM->>Sync: syncItem(savedItem)
    Sync->>FS: uploadItem(item)
    FS-->>Sync: cloudId
    Sync-->>VM: success(cloudId)
    VM->>Repo: update(item with cloudId)
    VM-->>UI: complete
```

---

## Conclusion
The designed system provides a practical hybrid architecture: fast local operations with Room and cloud continuity with Firebase/Drive integration. It meets the core objective of receipt/warranty management while remaining extensible for future features like analytics, multi-platform clients, and richer reporting.
