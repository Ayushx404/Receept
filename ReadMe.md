# Receipt Vault: Modern Asset & Warranty Management

**Receipt Vault** is a high-performance, modern Android application
built with Jetpack Compose. It empowers users to digitize, organize, and
track their receipts, warranties, and subscriptions in a secure,
cloud-synced environment.

------------------------------------------------------------------------

## 🚀 Key Features

-   **The Vault**: A centralized hub for managing all your assets
    (Receipts, Warranties, and Subscriptions).
-   **Intelligent Scanning**: Integrated **ML Kit Document Scanner** and
    **OCR** (Optical Character Recognition) to automatically extract
    information from physical receipts.
-   **Dual Cloud Sync**:
    -   **Firestore**: Real-time synchronization of metadata across
        devices.
    -   **Google Drive**: Secure backup of original receipt images and
        documents.
-   **Warranty Timeline**: Visualized timeline of asset lifespans with
    automated expiration alerts via **WorkManager**.
-   **Advanced Analytics**: Dedicated statistics dashboard providing
    insights into spending habits and asset categories.
-   **Data Portability**: Robust Import/Export functionality supporting
    both JSON and CSV formats.
-   **Modern Aesthetics**: Built with **Material 3**, featuring a
    dynamic theme system, smooth animations, and a premium dark mode
    experience.

------------------------------------------------------------------------

## 🛠️ Tech Stack

-   **UI**: Jetpack Compose (Material 3)\
-   **Language**: Kotlin (Coroutines, Flow)\
-   **Architecture**: MVVM with Dagger Hilt (Dependency Injection)\
-   **Database**: Room (Local Persistence)\
-   **Cloud Backend**: Firebase (Auth, Firestore)\
-   **API Integration**: Google Drive API v3\
-   **AI/ML**: ML Kit (Text Recognition & Document Scanner)\
-   **Networking**: Coil (Image Loading)\
-   **State Management**: DataStore (Preferences)

------------------------------------------------------------------------

## 📂 Project Structure

``` text
app/src/main/java/com/receiptwarranty/app/
├── data/           # Repositories, DAOs, Room DB, Cloud Sync logic
├── di/             # Hilt Dependency Injection modules
├── domain/         # Use cases and domain models
├── ui/             # UI Layer
│   ├── components/ # Reusable Compose components
│   ├── screens/    # Screen-level Composables (Home, Vault, Stats, etc.)
│   └── theme/      # Material 3 Design Tokens (Colors, Typography, Shapes)
├── viewmodel/      # Architecture-compliant ViewModels
└── util/           # Helper classes (Exporters, Connectivity, etc.)
```

------------------------------------------------------------------------

## ⚙️ Setup & Installation

### Prerequisites

-   Android Studio Ladybug or later\
-   JDK 17+\
-   A Firebase project with Google Sign-In and Firestore enabled\
-   Google Drive API enabled in the Google Cloud Console

### Steps

1.  **Clone the repository**:

    ``` bash
    git clone https://github.com/Ayushx404/Receept.git
    ```

2.  **Firebase Setup**:

    -   Add your `google-services.json` to the `app/` directory.

3.  **Google Sign-In**:

    -   Update the `default_web_client_id` in `res/values/strings.xml`
        with your OAuth client ID.

4.  **Build**:

    -   Sync Gradle and run the `:app` module on an Android 14+ (API 34)
        device.

------------------------------------------------------------------------

## 📄 License

Check the repository for license details.

------------------------------------------------------------------------

Built with ❤️ for better asset organization.
