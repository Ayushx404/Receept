# Receipt & Warranty Android App - Context Documentation

## 1. APP OVERVIEW

### App Name and Purpose
**Receipt Warranty Tracker** - An Android application for managing receipts and warranty cards with expiry reminders.

### Target Users
- Consumers who want to track purchase receipts for expense tracking or returns
- Users who need warranty expiration reminders to avoid missing coverage periods
- People who prefer local storage (no cloud sync, privacy-focused)

### Core Features
- Add receipts and warranty cards with photos
- Title and company/store identification for each item
- Full-text search by title, company, notes, or category
- Filter by warranty status (All, Receipts Only, Warranties Only, Expiring Soon, Expired)
- Category-based organization (Electronics, Appliances, Furniture, etc.)
- Warranty expiry reminders (configurable: 1 day, 3 days, 5 days, or 1 week before)
- Dashboard with statistics and quick actions
- CSV export functionality
- Material You / Samsung-style bold color theming

### Tech Stack
| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM with Repository Pattern |
| Database | Room (SQLite) |
| Navigation | Compose Navigation |
| Background Work | WorkManager |
| Image Loading | Coil |
| DI | Manual (AppContainer) |
| Min SDK | 34 (Android 14) |
| Target SDK | 35 |
| Build System | Gradle 8.9 + KSP |
| Compiler | Kotlin 1.9.x with Compose Compiler Plugin |

---

## 2. CURRENT PROBLEMS (CRITICAL)

### Database Migration Crash (Primary Issue)
**Status: CRITICAL - App fails to start on upgrade**

The migration from schema v1 to v2 is causing crashes on devices with existing data:

```kotlin
// MIGRATION_1_2 in AppDatabase.kt:35-53
database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN type TEXT DEFAULT 'WARRANTY'")
database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN category TEXT")
database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN reminderDays INTEGER")
database.execSQL("ALTER TABLE receipt_warranty RENAME COLUMN receiptImageUri TO imageUri")
database.execSQL("ALTER TABLE receipt_warranty DROP COLUMN warrantyImageUri")
```

**Issues:**
- SQLite ALTER TABLE limitations with column defaults
- Type conversion issues with existing NULL values
- Missing index creation causing query performance issues
- The rename operation may fail if `receiptImageUri` doesn't exist

**Error symptoms:**
- `IllegalStateException: Migration didn't properly handle entities`
- `SQLiteException: duplicate column name`
- App crashes immediately on launch after update

### Navigation Crashes
**Status: CRITICAL - Runtime crashes**

1. **Navigation Stack Issues** (`ReceiptWarrantyNavHost.kt:80-84`)
   ```kotlin
   popUpTo(Screen.Home.route) { inclusive = true }
   ```
   - Causes "destination already on back stack" warnings
   - Can lead to IllegalArgumentException on rapid navigation

2. **Type Parsing Crash** (`ReceiptWarrantyNavHost.kt:175-179`)
   ```kotlin
   val type = try { ReceiptType.valueOf(typeString) } catch (e: Exception) { ReceiptType.WARRANTY }
   ```
   - Empty or invalid type strings default silently
   - Can cause unexpected behavior if invalid type reaches database

3. **Detail Screen Item Not Found** (`DetailScreen.kt:58-61`)
   - Null item handling uses `LaunchedEffect` to navigate back
   - May cause visual flicker or navigation issues

4. **Dashboard Bottom Navigation** (`DashboardScreen.kt:81-102`)
   - Nested NavigationBar inside DashboardScreen
   - Redundant with main nav, causes state conflicts

### Notification Worker Crashes
**Status: CRITICAL - Runtime crashes**

`WarrantyReminderWorker.kt:48`:
```kotlin
NotificationManagerCompat.from(applicationContext).notify(id.toInt(), notification)
```

**Issues:**
- Missing POST_NOTIFICATIONS permission check (Android 13+)
- Notification channel not properly initialized
- ID collision with existing notifications
- Worker may run before app fully initialized

### ViewModel Type Safety Issues
**Status: MAJOR - Code smell, potential runtime errors**

`ReceiptWarrantyViewModel.kt:62-76`:
```kotlin
@Suppress("UNCHECKED_CAST")
val items = values[0] as List<ReceiptWarranty>
```

**Issues:**
- `@Suppress("UNCHECKED_CAST")` indicates unsafe type assertion
- `combine()` with 13 flows relies on positional indexing
- If flow order changes, runtime ClassCastException occurs
- No compile-time safety for combined flow types

---

## 3. TECHNICAL ARCHITECTURE

### Data Layer

#### Room Database Schema
```
receipt_warranty table:
- id: Long (Primary Key, Auto-generate)
- type: TEXT (RECEIPT or WARRANTY)
- title: TEXT (NOT NULL)
- company: TEXT (NOT NULL)
- category: TEXT (NULL allowed)
- imageUri: TEXT (NULL allowed, stored as file:// URI)
- purchaseDate: Long (NULL allowed, Unix timestamp)
- warrantyExpiryDate: Long (NULL allowed, Unix timestamp)
- reminderDays: INTEGER (NULL allowed, maps to ReminderDays enum)
- notes: TEXT (NULL allowed)
- createdAt: Long (NOT NULL, Unix timestamp)
```

#### Entities

**ReceiptWarranty.kt:18-51**
```kotlin
@Entity(tableName = "receipt_warranty")
data class ReceiptWarranty(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: ReceiptType,
    val title: String,
    val company: String,
    val category: String? = null,
    val imageUri: String? = null,
    val purchaseDate: Long? = null,
    val warrantyExpiryDate: Long? = null,
    val reminderDays: ReminderDays? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun warrantyStatus(): WarrantyStatus { ... }
}
```

#### DAOs (Data Access Objects)

**ReceiptWarrantyDao.kt** provides:
- `getAll()`: Flow<List<ReceiptWarranty>> - All items ordered by creation date
- `getById(id)`: Flow<ReceiptWarranty?> - Single item by ID
- `getAllCompanies()`: Flow<List<String>> - Distinct company names for autocomplete
- `getAllCategories()`: Flow<List<String>> - Distinct categories
- `getByCategory(category)`: Flow<List<ReceiptWarranty>> - Filtered by category
- `getCategoryStats()`: Flow<List<CategoryCount>> - Category counts
- `getReceiptCount()`: Flow<Int> - Count of RECEIPT type
- `getWarrantyCount()`: Flow<Int> - Count of WARRANTY type
- `getActiveWarrantyCount()`: Flow<Int> - Warranties with future expiry
- `getExpiringSoonCount()`: Flow<Int> - Within 7 days of expiry
- `getExpiredWarrantyCount()`: Flow<Int> - Past expiry date
- `getExpiringSoonItems()`: Flow<List<ReceiptWarranty>> - Items expiring in 30 days
- `getItemsWithReminders()`: Flow<List<ReceiptWarranty>> - Items with reminder set
- `getAllItemsForExport()`: suspend List<ReceiptWarranty> - For CSV export
- `insert(item)`: suspend Long - Returns generated ID
- `update(item)`: suspend Unit
- `delete(item)`: suspend Unit
- `deleteById(id)`: suspend Unit

### Repository Pattern

**ReceiptWarrantyRepository.kt** wraps DAO with:
- `search(query)`: Filters all items by title, company, notes, or category
- `filterByWarrantyStatus(items, filter)`: Applies WarrantyFilter
- `filterByCategory(items, category)`: Filters by category
- `exportToCSV()`: Generates CSV string from all items
- All DAO methods exposed as Flow or suspend functions

### ViewModel with StateFlow

**ReceiptWarrantyViewModel.kt** manages UI state:
- Combines 13 Flow sources using `combine()`
- Single `StateFlow<ReceiptWarrantyUiState>` for entire UI
- Actions: `updateSearchQuery()`, `setWarrantyFilter()`, `setCategoryFilter()`
- CRUD: `insertOrUpdate()`, `delete()`, `deleteById()`
- Export: `exportToCSV(context)` - Shares CSV via intent
- Reminder scheduling integrated with insert/update/delete

### Navigation with Compose Navigation

**Screen Routes:**
```kotlin
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Dashboard : Screen("dashboard")
    data object Settings : Screen("settings")
    data object Add : Screen("add/{type}") {
        fun createRoute(type: String) = "add/$type"
    }
    data object Edit : Screen("edit/{id}") {
        fun createRoute(id: Long) = "edit/$id"
    }
    data object Detail : Screen("detail/{id}") {
        fun createRoute(id: Long) = "detail/$id"
    }
}
```

**Navigation Structure:**
- Bottom Navigation Bar: Home, Dashboard, Settings
- FAB opens TypeSelectionBottomSheet for add selection
- Add screen accepts type parameter (RECEIPT/WARRANTY)
- Detail screen shows full item, allows edit/delete

### WorkManager for Reminders

**WarrantyReminderScheduler.kt:**
```kotlin
fun scheduleReminder(item: ReceiptWarranty) {
    val expiry = item.warrantyExpiryDate ?: return
    val reminderDays = item.reminderDays ?: ReminderDays.ONE_WEEK
    val reminderTime = expiry - (reminderDays.days * 24 * 60 * 60 * 1000L)
    
    val workRequest = OneTimeWorkRequestBuilder<WarrantyReminderWorker>()
        .setInputData(Data.Builder()
            .putString(KEY_TITLE, item.title)
            .putLong(KEY_ITEM_ID, item.id)
            .putInt(KEY_DAYS_BEFORE, reminderDays.days)
            .build())
        .setInitialDelay(reminderTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        .addTag("warranty_reminder_${item.id}")
        .build()
    
    WorkManager.getInstance(context).enqueue(workRequest)
}

fun cancelReminder(itemId: Long) {
    WorkManager.getInstance(context).cancelAllWorkByTag("warranty_reminder_$itemId")
}
```

**WarrantyReminderWorker.kt:**
- Extends `CoroutineWorker`
- Reads input data (title, item ID, days before)
- Creates notification with PendingIntent to MainActivity
- Returns Result.success() or Result.failure()

---

## 4. SCHEMA CHANGES MADE

### Old Schema (v1)
```
receipt_warranty table:
- id: INTEGER PRIMARY KEY
- title: TEXT NOT NULL
- company: TEXT NOT NULL
- receiptImageUri: TEXT
- warrantyImageUri: TEXT
- purchaseDate: INTEGER
- expiryDate: INTEGER
- notes: TEXT
- createdAt: INTEGER NOT NULL
```

**Limitations of v1:**
- No type distinction between receipts and warranties
- Separate image columns (redundant, confusing)
- No category support
- No configurable reminders
- No index on frequently queried columns

### New Schema (v2)
```
receipt_warranty table:
- id: INTEGER PRIMARY KEY AUTOINCREMENT
- type: TEXT NOT NULL (CHECK type IN ('RECEIPT', 'WARRANTY'))
- title: TEXT NOT NULL
- company: TEXT NOT NULL
- category: TEXT
- imageUri: TEXT (consolidated from receiptImageUri + warrantyImageUri)
- purchaseDate: INTEGER
- warrantyExpiryDate: INTEGER (renamed from expiryDate)
- reminderDays: INTEGER (maps to ReminderDays enum)
- notes: TEXT
- createdAt: INTEGER NOT NULL

Indexes:
- idx_category ON receipt_warranty(category)
```

### Migration Failures and Fixes Needed

**Problem 1: ALTER TABLE with Defaults**
```sql
-- This may fail on some SQLite versions:
ALTER TABLE receipt_warranty ADD COLUMN type TEXT DEFAULT 'WARRANTY'

-- Fix: Use safer migration without defaults
-- OR: Handle NULL values explicitly
```

**Problem 2: Column Rename/Rename Issues**
```sql
-- If receiptImageUri doesn't exist (null entries):
ALTER TABLE receipt_warranty RENAME COLUMN receiptImageUri TO imageUri

-- Fix: Check column existence before rename
-- OR: Create new column and copy data
```

**Problem 3: Missing Type Constraints**
```sql
-- No CHECK constraint on type column
-- Fix: Add validation in migration
database.execSQL("CREATE TABLE receipt_warranty_new (... CHECK(type IN ('RECEIPT', 'WARRANTY')))")
database.execSQL("INSERT INTO receipt_warranty_new SELECT * FROM receipt_warranty")
database.execSQL("DROP TABLE receipt_warranty")
database.execSQL("ALTER TABLE receipt_warranty_new RENAME TO receipt_warranty")
```

---

## 5. CRITICAL FIXES REQUIRED (Priority Order)

### Phase 1: Database Migration Fix (Nuclear Option)
**Priority: CRITICAL - Blocks app startup**

**Option A: Nuclear Migration (Recommended for fresh start)**
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create new table with correct schema
        database.execSQL("""
            CREATE TABLE receipt_warranty_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL CHECK(type IN ('RECEIPT', 'WARRANTY')),
                title TEXT NOT NULL,
                company TEXT NOT NULL,
                category TEXT,
                imageUri TEXT,
                purchaseDate INTEGER,
                warrantyExpiryDate INTEGER,
                reminderDays INTEGER,
                notes TEXT,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())
        
        // Migrate data with type conversion
        database.execSQL("""
            INSERT INTO receipt_warranty_new (id, type, title, company, category, imageUri, 
                purchaseDate, warrantyExpiryDate, reminderDays, notes, createdAt)
            SELECT 
                id,
                COALESCE(type, 'WARRANTY'),
                title,
                company,
                category,
                COALESCE(receiptImageUri, warrantyImageUri),
                purchaseDate,
                expiryDate,
                NULL,
                notes,
                createdAt
            FROM receipt_warranty
        """.trimIndent())
        
        // Drop old table and rename
        database.execSQL("DROP TABLE receipt_warranty")
        database.execSQL("ALTER TABLE receipt_warranty_new RENAME TO receipt_warranty")
        
        // Create indexes
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_category ON receipt_warranty(category)")
    }
}
```

**Option B: Fallback Strategy (For existing data preservation)**
```kotlin
// Add fallback to recreate database if migration fails
fun getInstance(context: Context): AppDatabase {
    return INSTANCE ?: synchronized(this) {
        try {
            // Try normal migration first
            INSTANCE ?: createMigratedDatabase(context)
        } catch (e: Exception) {
            // Nuclear option on failure
            context.deleteDatabase(DATABASE_NAME)
            createMigratedDatabase(context)
        }
    }
}

private fun createMigratedDatabase(context: Context): AppDatabase {
    val instance = Room.databaseBuilder(...)
        .addMigrations(MIGRATION_1_2)
        .build()
    INSTANCE = instance
    return instance
}
```

### Phase 2: Navigation Fixes
**Priority: CRITICAL - Runtime crashes**

**Fix 1: Safe Navigation with proper popUpTo**
```kotlin
// Replace:
navController.navigate(Screen.Home.route) {
    popUpTo(Screen.Home.route) { inclusive = true }
}

// With:
navController.navigate(Screen.Home.route) {
    popUpTo(Screen.Dashboard.route) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
```

**Fix 2: Type Parameter Validation**
```kotlin
composable(
    route = Screen.Add.route,
    arguments = listOf(navArgument("type") { 
        type = NavType.StringType
        defaultValue = "WARRANTY"
    })
) { backStackEntry ->
    val typeString = backStackEntry.arguments?.getString("type") ?: "WARRANTY"
    val type = try {
        ReceiptType.valueOf(typeString.uppercase())
    } catch (e: IllegalArgumentException) {
        // Log error and use safe default
        ReceiptType.WARRANTY
    }
    // ...
}
```

**Fix 3: Remove Nested NavigationBar**
- DashboardScreen.kt should not have its own NavigationBar
- Navigation is handled by main NavHost scaffold

**Fix 4: Safe Item Retrieval**
```kotlin
composable(Screen.Detail.route) { backStackEntry ->
    val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
    if (id == null) {
        navController.popBackStack()
        return@composable
    }
    val item = uiState.items.find { it.id == id }
    if (item == null) {
        // Item might not be in filtered list
        // Consider fetching directly via repository
        LaunchedEffect(id) {
            repository.getById(id).collect { fetchedItem ->
                if (fetchedItem != null) {
                    // Navigate to detail with item
                } else {
                    navController.popBackStack()
                }
            }
        }
        return@composable
    }
    // Show detail...
}
```

### Phase 3: Notification Worker Fix
**Priority: CRITICAL - Permission crash on Android 14+**

**Fix 1: Permission Check Before Notification**
```kotlin
override suspend fun doWork(): Result {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            return Result.failure()
        }
    }
    // ... rest of notification logic
}
```

**Fix 2: Unique Notification IDs**
```kotlin
// Use item ID + offset to avoid collisions
NotificationManagerCompat.from(applicationContext)
    .notify(NOTIFICATION_BASE_ID + id.toInt(), notification)

companion object {
    private const val NOTIFICATION_BASE_ID = 10000
}
```

**Fix 3: Handle Worker Initialization**
- Ensure WorkManager is initialized after Application.onCreate()
- Use lazy initialization in AppContainer

### Phase 4: ViewModel Refactoring
**Priority: MAJOR - Type safety and maintainability**

**Current Problem:**
```kotlin
@Suppress("UNCHECKED_CAST")
val items = values[0] as List<ReceiptWarranty> // UNSAFE!
```

**Solution: Typed Combine**
```kotlin
// Instead of combining all flows, use separate StateFlows
private val _searchQuery = MutableStateFlow("")
val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

private val _warrantyFilter = MutableStateFlow(WarrantyFilter.ALL)
val warrantyFilter: StateFlow<WarrantyFilter> = _warrantyFilter.asStateFlow()

// Derived state for filtered items
val filteredItems: StateFlow<List<ReceiptWarranty>> = combine(
    repository.getAll(),
    _searchQuery,
    _warrantyFilter,
    _categoryFilter
) { items, query, filter, category ->
    // Apply filters with type safety
    var result = items
    
    if (query.isNotBlank()) {
        result = result.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.company.contains(query, ignoreCase = true)
        }
    }
    
    result = when (filter) {
        WarrantyFilter.ALL -> result
        WarrantyFilter.ALL_RECEIPTS -> result.filter { it.type == ReceiptType.RECEIPT }
        WarrantyFilter.ALL_WARRANTIES -> result.filter { it.type == ReceiptType.WARRANTY }
        WarrantyFilter.EXPIRING_SOON -> result.filter {
            it.type == ReceiptType.WARRANTY && it.warrantyStatus() == WarrantyStatus.EXPIRING_SOON
        }
        WarrantyFilter.EXPIRED -> result.filter {
            it.type == ReceiptType.WARRANTY && it.warrantyStatus() == WarrantyStatus.EXPIRED
        }
    }
    
    if (category != null) {
        result = result.filter { it.category == category }
    }
    
    result
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
)
```

---

## 6. FILES OVERVIEW

### Data Layer

| File | Purpose | Key Content |
|------|---------|-------------|
| `ReceiptWarranty.kt` | Entity definition | ReceiptWarranty data class, enums (ReceiptType, ReminderDays, WarrantyStatus) |
| `AppDatabase.kt` | Room database | Database configuration, DAO access, Migration_1_2 |
| `ReceiptWarrantyDao.kt` | Data access | All CRUD queries, statistics queries |
| `ReceiptWarrantyRepository.kt` | Repository | Business logic, filtering, CSV export |
| `AppContainer.kt` | DI container | Database, Repository, Scheduler singletons |

### ViewModel

| File | Purpose | Key Content |
|------|---------|-------------|
| `ReceiptWarrantyViewModel.kt` | Main ViewModel | UiState, combined flows, actions |

### UI - Screens

| File | Purpose | Navigation Route | Key Features |
|------|---------|------------------|--------------|
| `HomeScreen.kt` | Main list | home | Search, filter chips, item list |
| `DashboardScreen.kt` | Statistics | dashboard | Stats cards, quick add, category breakdown |
| `AddEditScreen.kt` | Add/Edit form | add/{type}, edit/{id} | Form with validation, image picker, date pickers |
| `DetailScreen.kt` | Item details | detail/{id} | Full info, edit/delete actions |
| `SettingsScreen.kt` | App settings | settings | Export, about |

### UI - Components

| File | Purpose | Usage |
|------|---------|-------|
| `ItemCard.kt` | List item card | HomeScreen LazyColumn |
| `DashboardComponents.kt` | Dashboard widgets | StatCard, ExpiringSoonCard, CategoryStatRow, StatusChip |
| `CategorySelector.kt` | Category dropdown | AddEditScreen |
| `ReminderSelector.kt` | Reminder chips | AddEditScreen |
| `Material3DatePickerDialog.kt` | Date picker | AddEditScreen |
| `TypeSelectionBottomSheet.kt` | Add type dialog | FAB menu |

### UI - Theme

| File | Purpose |
|------|---------|
| `Theme.kt` | Material3 theming, color schemes |
| `Type.kt` | Typography definitions |

### Workers

| File | Purpose |
|------|---------|
| `WarrantyReminderWorker.kt` | Background notification worker |
| `WarrantyReminderScheduler.kt` | WorkManager scheduling |

### Utilities

| File | Purpose |
|------|---------|
| `ImageFileManager.kt` | Image file handling, gallery to local storage |

### Entry Points

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Activity, permission handling |
| `ReceiptWarrantyApp.kt` | Application class, notification channel setup |
| `ReceiptWarrantyNavHost.kt` | Main navigation graph, scaffold with bottom nav |

### Dependency Graph

```
AppContainer
├── AppDatabase
│   └── ReceiptWarrantyDao
├── ReceiptWarrantyRepository
│   └── ReceiptWarrantyDao (queries)
└── WarrantyReminderScheduler
    └── WorkManager

ReceiptWarrantyViewModel (Factory)
├── ReceiptWarrantyRepository
└── WarrantyReminderScheduler

ReceiptWarrantyNavHost
├── ReceiptWarrantyViewModel
├── Screens (Home, Dashboard, Settings, Add, Edit, Detail)
└── Components (TypeSelectionBottomSheet, etc.)
```

---

## 7. IMPLEMENTATION NOTES

### Compose Compiler Plugin Setup

**build.gradle.kts (app):**
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") // Kotlin 2.0+ unified compiler
    id("com.google.devtools.ksp") // For Room annotation processing
}

android {
    compileSdk = 35
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15" // Set by plugin.compose
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
}
```

**Note:** Using `org.jetbrains.kotlin.plugin.compose` (Kotlin 2.0) instead of `kotlinCompilerExtensionVersion` in composeOptions.

### Material3 Theming

**Color System:**
```kotlin
private val LightColorScheme = lightColorScheme(
    primary = Teal,              // Main action color
    onPrimary = Color.White,
    primaryContainer = TealLight,
    secondary = TealDark,         // Secondary actions
    tertiary = TealAccent,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    error = Red
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFF4DB6AC),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = Color(0xFFCF6679)
)
```

**Badge Colors:**
```kotlin
object WarrantyBadgeColors {
    val valid = Color(0xFF4CAF50)      // Green - Active warranty
    val expiringSoon = Amber           // Orange/Yellow - <7 days
    val expired = Red                 // Red - Past expiry
}
```

### Category System

**Default Categories:**
```kotlin
val ALL = listOf(
    "Electronics", "Appliances", "Furniture", "Vehicles",
    "Clothing", "Sports Equipment", "Tools", "Books",
    "Gaming", "Health & Beauty", "Home & Garden", "Other"
)
```

**Features:**
- User can select from defaults or create custom categories
- Custom categories are user-defined strings
- Categories stored as nullable TEXT in database
- Statistics calculated by GROUP BY category

### Reminder System

**Reminder Options:**
```kotlin
enum class ReminderDays(val days: Int, val displayName: String) {
    ONE_DAY(1, "1 day before"),
    THREE_DAYS(3, "3 days before"),
    FIVE_DAYS(5, "5 days before"),
    ONE_WEEK(7, "1 week before")
}
```

**Behavior:**
- Only applies to WARRANTY type items
- Triggers WorkManager job at calculated time
- Notification shown when worker runs
- User can toggle reminder on/off

### Export Functionality

**CSV Format:**
```csv
id,type,title,company,category,purchase_date,expiry_date,reminder,notes,created_at
```

**Implementation:**
```kotlin
private fun buildCSV(items: List<ReceiptWarranty>): String {
    val sb = StringBuilder()
    sb.appendLine("id,type,title,company,category,purchase_date,expiry_date,reminder,notes,created_at")
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    items.forEach { item ->
        sb.appendLine(
            "${item.id}," +
            "${item.type}," +
            "\"${item.title.replace("\"", "\"\"")}\"," +
            // ... all fields with proper escaping
        )
    }
    
    return sb.toString()
}
```

**Sharing:**
- Uses `Intent.ACTION_SEND` with `text/csv` MIME type
- System chooser lets user select email/app
- Subject: "Receipt & Warranty Export"

---

## 8. DESIGN SYSTEM

### Material You / Samsung-style Bold Colors

**Primary Color (Teal):**
```kotlin
private val Teal = Color(0xFF079992)
private val TealDark = Color(0xFF047A74)
```

**Design Principles:**
- Bold, saturated primary colors
- High contrast for readability
- Status colors use semantic meaning (red=error, amber=warning)
- Surface colors maintain hierarchy through elevation

### Bottom Navigation

**Structure:**
```
Bottom Navigation Bar (Material3 NavigationBar)
├── Home (Icon: Home)      → Screen.Home
├── Dashboard (Icon: Dashboard) → Screen.Dashboard
└── Settings (Icon: Settings)    → Screen.Settings
```

**Implementation:**
```kotlin
NavigationBar {
    NavigationBarItem(
        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
        label = { Text("Home") },
        selected = navController.currentDestination?.route == Screen.Home.route,
        onClick = { /* Navigate if not already there */ }
    )
    // ... other items
}
```

### FAB Expansion for Type Selection

**Behavior:**
1. User taps FAB (+)
2. TypeSelectionBottomSheet appears (ModalBottomSheet)
3. User selects Receipt or Warranty
4. Navigation to Add screen with type parameter

**TypeSelectionBottomSheet:**
```kotlin
@Composable
fun TypeSelectionBottomSheet(
    onTypeSelected: (ReceiptType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet { ... }
}
```

**Cards:**
- Receipt card: Primary container color, Receipt icon
- Warranty card: Secondary container color, Verified icon

### Dashboard Statistics Display

**Quick Stats Section:**
```kotlin
Row {
    StatCard(icon = Icons.Default.Receipt, label = "Receipts", value = receiptCount)
    StatCard(icon = Icons.Default.Verified, label = "Warranties", value = warrantyCount)
}
Row {
    StatCard(icon = Icons.Default.CheckCircle, label = "Active", value = activeCount)
    StatCard(icon = Icons.Default.Warning, label = "Expiring", value = expiringCount)
}
```

**Components:**
- StatCard: Icon, label, value with colored background
- ExpiringSoonCard: Item title, company, days remaining badge
- CategoryStatRow: Category name, count, arrow indicator

---

## 9. DATABASE ENTITY DETAILS

### ReceiptWarranty Entity Fields

| Field | Type | Nullable | Purpose |
|-------|------|----------|---------|
| `id` | Long | No | Primary key, auto-generated |
| `type` | ReceiptType | No | RECEIPT or WARRANTY distinction |
| `title` | String | No | Item name/title |
| `company` | String | No | Store/manufacturer name |
| `category` | String? | Yes | User category (Electronics, etc.) |
| `imageUri` | String? | Yes | Photo file URI |
| `purchaseDate` | Long? | Yes | Unix timestamp of purchase |
| `warrantyExpiryDate` | Long? | Yes | Unix timestamp of warranty expiry |
| `reminderDays` | ReminderDays? | Yes | Reminder configuration |
| `notes` | String? | Yes | Additional notes |
| `createdAt` | Long | No | Unix timestamp of creation |

### Enums

**ReceiptType:**
```kotlin
enum class ReceiptType {
    RECEIPT,
    WARRANTY
}
```

**ReminderDays:**
```kotlin
enum class ReminderDays(val days: Int, val displayName: String) {
    ONE_DAY(1, "1 day before"),
    THREE_DAYS(3, "3 days before"),
    FIVE_DAYS(5, "5 days before"),
    ONE_WEEK(7, "1 week before")
}
```

**WarrantyStatus:**
```kotlin
enum class WarrantyStatus {
    VALID,           // Expiry > 7 days from now
    EXPIRING_SOON,   // Expiry within 7 days
    EXPIRED,         // Expiry < now
    NO_WARRANTY      // Null expiry date
}
```

### Category System

**Default Categories:**
1. Electronics - Phones, computers, gadgets
2. Appliances - Home appliances, kitchen tools
3. Furniture - Tables, chairs, beds
4. Vehicles - Cars, bikes, parts
5. Clothing - Apparel, accessories
6. Sports Equipment - Sports gear, fitness
7. Tools - Hand tools, power tools
8. Books - Books, media
9. Gaming - Games, consoles
10. Health & Beauty - Cosmetics, health products
11. Home & Garden - Home decor, garden
12. Other - Everything else

**Storage:**
- Categories stored as TEXT in `category` column
- Null means no category assigned
- User can create custom categories (free-form string)
- Query with index for performance: `CREATE INDEX idx_category ON receipt_warranty(category)`

---

## 10. TESTING CHECKLIST

### Critical Path Tests

**Database Operations:**
- [ ] Insert receipt with all fields
- [ ] Insert warranty with all fields
- [ ] Update existing item
- [ ] Delete item by ID
- [ ] Query all items sorted by createdAt
- [ ] Query item by ID
- [ ] Search by title (case insensitive)
- [ ] Search by company
- [ ] Search by notes
- [ ] Filter by type (receipts/warranties)
- [ ] Filter by category
- [ ] Filter by warranty status (expiring/expired)

**ViewModel Tests:**
- [ ] Initial state is empty
- [ ] Search query filters results
- [ ] Warranty filter changes displayed items
- [ ] Category filter combines with warranty filter
- [ ] Insert updates StateFlow
- [ ] Delete updates StateFlow
- [ ] Export generates valid CSV

### Navigation Tests

**Home Screen:**
- [ ] FAB opens type selection
- [ ] Filter chips toggle correctly
- [ ] Empty state shows when no items
- [ ] Search bar filters list
- [ ] Clicking item navigates to detail

**Add/Edit Screen:**
- [ ] Form validation prevents save when required fields missing
- [ ] Image picker saves image to local storage
- [ ] Date picker sets purchase date
- [ ] Category selector shows options
- [ ] Cancel returns to previous screen
- [ ] Save persists to database

**Detail Screen:**
- [ ] Shows all item fields
- [ ] Edit button navigates to edit
- [ ] Delete removes item and returns
- [ ] Back button returns to home

**Dashboard:**
- [ ] Statistics cards show correct counts
- [ ] Quick add buttons navigate correctly
- [ ] Expiring soon section shows items
- [ ] Category section shows breakdown

### Database Migration Tests

**Fresh Install (v2 only):**
- [ ] App launches without errors
- [ ] Can insert new items
- [ ] Default categories work
- [ ] Reminders schedule correctly

**Upgrade from v1:**
- [ ] App launches without crash
- [ ] Existing items display correctly
- [ ] Type field defaults to WARRANTY
- [ ] Image URIs migrated correctly
- [ ] Categories work (may be null for old items)
- [ ] Reminders work (may be null for old items)

**Migration Edge Cases:**
- [ ] Items with null purchase dates
- [ ] Items with null expiry dates
- [ ] Items with special characters in title/notes
- [ ] Large dataset (100+ items)

### Notification Tests

**Permission:**
- [ ] Notification shown on Android 14+ with permission granted
- [ ] No crash when permission denied
- [ ] Permission request at app start

**Worker:**
- [ ] Scheduled reminder fires at correct time
- [ ] Notification shows correct title and message
- [ ] Tapping notification opens app
- [ ] Multiple reminders don't collide
- [ ] Cancelled item cancels scheduled reminder

**Edge Cases:**
- [ ] Item deleted before reminder fires
- [ ] App force stopped before reminder
- [ ] Device restarted before reminder

---

## 11. COMMON ISSUES & SOLUTIONS

### Compilation Errors

**Error: "Unresolved reference: rememberNavController"**
```
Solution: Import correct package
import androidx.navigation.compose.rememberNavController
```

**Error: "Type inference failed" in combine()**
```
Solution: Ensure all flows have explicit types
combine(flow1, flow2, flow3) { a, b, c -> ... }
```

**Error: "Composable annotation not applied"**
```
Solution: Ensure function is @Composable and called from composable context
```

**Error: "Room schema export"**
```
Solution: Set exportSchema = false in @Database annotation
@Database(entities = [ReceiptWarranty::class], version = 2, exportSchema = false)
```

**Error: "Activity Result API mismatch"**
```
Solution: Use correct ActivityResultLauncher
val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri -> ... }
```

### Runtime Crashes

**Crash: "IllegalStateException: Cannot perform this operation"**
```
Cause: Database operation on main thread
Solution: Use coroutines/flows for database operations
```

**Crash: "NullPointerException" on navigation**
```
Cause: navController.currentDestination is null
Solution: Use safe navigation with null checks
```

**Crash: "ClassCastException" in ViewModel**
```
Cause: combine() with wrong number of flows
Solution: Verify flow count matches combine() parameters
```

**Crash: "Notification not posted" on Android 14+**
```
Cause: Missing POST_NOTIFICATIONS permission
Solution: Check permission before notifying
```

**Crash: "Worker crash"**
```
Cause: WorkManager initialization issue
Solution: Use lazy initialization in Application
```

### Navigation Issues

**Issue: Back stack contains multiple Home screens**
```
Solution: Use proper popUpTo strategy
navController.navigate(route) {
    popUpTo(Screen.Home.route) { inclusive = false }
    launchSingleTop = true
}
```

**Issue: FAB doesn't respond after navigation**
```
Solution: State is reset when NavHost recomposes
Use remember { mutableStateOf(false) } at NavHost level
```

**Issue: Deep link to detail shows empty screen**
```
Cause: uiState.items doesn't contain the item yet
Solution: Fetch item by ID from repository directly, not from uiState
```

**Issue: Bottom navigation indicator doesn't update**
```
Solution: Compare currentDestination.route with screen.route
selected = navController.currentDestination?.route == Screen.Home.route
```

---

## APP_CONTEXT.md

This document provides comprehensive context for the Receipt Warranty Tracker Android app. It covers the complete architecture, current issues, migration details, and implementation notes necessary for understanding and maintaining the codebase.

**Last Updated:** February 2026
**Maintained By:** Development Team
