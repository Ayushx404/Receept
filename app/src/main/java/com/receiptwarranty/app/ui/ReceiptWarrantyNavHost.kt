@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.receiptwarranty.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.receiptwarranty.app.ui.theme.VaultShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.ui.components.TypeSelectionBottomSheet
import com.receiptwarranty.app.ui.screens.AddEditScreen
import com.receiptwarranty.app.ui.screens.CategoryDetailScreen
import com.receiptwarranty.app.ui.screens.DashboardScreen
import com.receiptwarranty.app.ui.screens.DetailScreen
import com.receiptwarranty.app.ui.screens.ExpiringSoonScreen
import com.receiptwarranty.app.ui.screens.HomeScreen
import com.receiptwarranty.app.ui.screens.ImportScreen
import com.receiptwarranty.app.ui.screens.OnboardingScreen
import com.receiptwarranty.app.ui.screens.PrivacyScreen
import com.receiptwarranty.app.ui.screens.AboutMeScreen
import com.receiptwarranty.app.ui.screens.SettingsScreen
import com.receiptwarranty.app.ui.screens.StatsScreen
import com.receiptwarranty.app.ui.screens.ArchiveScreen
import com.receiptwarranty.app.ui.screens.AdvancedSettingsScreen
import com.receiptwarranty.app.BuildConfig
import com.receiptwarranty.app.util.PreferenceKeys
import com.receiptwarranty.app.util.ExportDestination
import com.receiptwarranty.app.viewmodel.AddEditViewModel
import com.receiptwarranty.app.viewmodel.DashboardViewModel
import com.receiptwarranty.app.viewmodel.DataManagementViewModel
import com.receiptwarranty.app.viewmodel.DetailViewModel
import com.receiptwarranty.app.viewmodel.HomeViewModel
import com.receiptwarranty.app.viewmodel.SettingsViewModel
import com.receiptwarranty.app.viewmodel.StatsViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Dashboard : Screen("dashboard")
    data object Settings : Screen("settings")
    data object Import : Screen("import")
    data object Add : Screen("add/{type}") {
        fun createRoute(type: String) = "add/$type"
    }
    data object Edit : Screen("edit/{id}") {
        fun createRoute(id: Long) = "edit/$id"
    }
    data object Detail : Screen("detail/{id}") {
        fun createRoute(id: Long) = "detail/$id"
    }
    data object ExpiringSoon : Screen("expiring_soon")
    data object Timeline : Screen("warranty_timeline")
    data object CategoryDetail : Screen("category/{name}") {
        fun createRoute(name: String) = "category/${android.net.Uri.encode(name)}"
    }
    data object Onboarding : Screen("onboarding")
    data object Stats : Screen("stats")
    data object Privacy : Screen("privacy")
    data object AboutMe : Screen("about_me")
    data object Archive : Screen("archive")
    data object AdvancedSettings : Screen("advanced_settings")
}

private val bottomNavRoutes = listOf(Screen.Home.route, Screen.Dashboard.route)

@Composable
fun ReceiptWarrantyNavHost(
    navController: NavHostController = rememberNavController(),
    userId: String = "local",
    userEmail: String = "",
    userName: String? = null,
    userProfilePicture: String? = null,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    // ── Feature ViewModels ──────────────────────────────────────────────────
    val homeViewModel: HomeViewModel = hiltViewModel()
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val addEditViewModel: AddEditViewModel = hiltViewModel()
    val detailViewModel: DetailViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val statsViewModel: StatsViewModel = hiltViewModel()
    val dataManagementViewModel: DataManagementViewModel = hiltViewModel()

    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val appearanceSettings by settingsViewModel.appearanceSettings.collectAsStateWithLifecycle()
    val currencySymbol = appearanceSettings.currencySymbol

    var showAddTypeSheet by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }

    // ── Correct bottom-nav selected state via backstack ─────────────────────
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavRoutes
    val showFab = currentRoute == Screen.Home.route && !homeUiState.isSelectionMode

    // ── Snackbar for undo-delete ─────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect undo events from HomeViewModel (multi-delete)
    LaunchedEffect(homeViewModel) {
        homeViewModel.undoDeleteEvent.collect { count ->
            val result = snackbarHostState.showSnackbar(
                message = if (count == 1) "1 item deleted" else "$count items deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                homeViewModel.undoDelete()
            }
        }
    }

    // Collect undo events from DetailViewModel (single delete)
    LaunchedEffect(detailViewModel) {
        detailViewModel.undoDeleteEvent.collect { event ->
            val result = snackbarHostState.showSnackbar(
                message = "\"${event.itemTitle}\" deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                detailViewModel.undoDelete()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", style = MaterialTheme.typography.labelMedium) },
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", style = MaterialTheme.typography.labelMedium) },
                        selected = currentRoute == Screen.Dashboard.route,
                        onClick = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { showAddTypeSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = VaultShape.small
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { paddingValues ->
        // Use SharedPreferences to check if onboarding is needed
        val prefs = context.getSharedPreferences(PreferenceKeys.APP_PREFS, android.content.Context.MODE_PRIVATE)
        val hasSeenOnboarding = prefs.getBoolean(PreferenceKeys.HAS_SEEN_ONBOARDING, false)
        val startDest = if (hasSeenOnboarding) Screen.Home.route else Screen.Onboarding.route

        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.9f, animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.9f, animationSpec = tween(300))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.9f, animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.9f, animationSpec = tween(300))
            }
        ) {
            // ── Onboarding ───────────────────────────────────────────────────
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Home ─────────────────────────────────────────────────────────
            composable(Screen.Home.route) {
                var showShareSheet by remember { mutableStateOf(false) }

                HomeScreen(
                    uiState = homeUiState,
                    onSearchQueryChange = homeViewModel::updateSearchQuery,
                    onWarrantyFilterChange = homeViewModel::setWarrantyFilter,
                    onTagToggle = homeViewModel::toggleTag,
                    onItemClick = { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    },
                    onEnterSelectionMode = homeViewModel::enterSelectionMode,
                    onEnterSelectionModeEmpty = homeViewModel::enterSelectionModeEmpty,
                    onSelectItem = homeViewModel::toggleSelection,
                    onExitSelectionMode = homeViewModel::exitSelectionMode,
                    onDeleteSelected = homeViewModel::deleteSelectedItems,
                    onShareSelected = { showShareSheet = true },
                    onRetrySync = { settingsViewModel.syncNow() },
                    onSync = { settingsViewModel.syncNow() }
                )

                if (showShareSheet) {
                    val shareResult by homeViewModel.shareCardResult.collectAsStateWithLifecycle()
                    com.receiptwarranty.app.ui.components.SharePreviewBottomSheet(
                        shareResult = shareResult,
                        onGenerate = { theme -> homeViewModel.generateShareCard(context, theme) },
                        onDismiss = { showShareSheet = false }
                    )
                }
            }

            // ── Dashboard ────────────────────────────────────────────────────
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    uiState = dashboardUiState,
                    currencySymbol = currencySymbol,
                    onNavigateToExpiringSoon = {
                        navController.navigate(Screen.ExpiringSoon.route)
                    },
                    onNavigateToCategory = { categoryName ->
                        navController.navigate(Screen.CategoryDetail.createRoute(categoryName))
                    },
                    onItemClick = { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    },
                    onAddClick = { type ->
                        navController.navigate(Screen.Add.createRoute(type.name))
                    },
                    onPayClick = { item ->
                        dashboardViewModel.markAsPaid(item)
                    },
                    onExportCSV = {
                        homeViewModel.exportToCSV(context)
                    },
                    onNavigateToTimeline = {
                        navController.navigate(Screen.Timeline.route)
                    },
                    onNavigateToStats = {
                        navController.navigate(Screen.Stats.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // ── Expiring Soon ─────────────────────────────────────────────────
            composable(Screen.ExpiringSoon.route) {
                ExpiringSoonScreen(
                    viewModel = dashboardViewModel,
                    onBack = { navController.popBackStack() },
                    onItemClick = { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    }
                )
            }

            // ── Warranty Timeline ─────────────────────────────────────────────
            composable(Screen.Timeline.route) {
                val items by homeViewModel.allItems.collectAsStateWithLifecycle()
                com.receiptwarranty.app.ui.screens.WarrantyTimelineScreen(
                    items = items,
                    onItemClick = { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Category Detail ───────────────────────────────────────────────
            composable(
                route = Screen.CategoryDetail.route,
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { backStackEntry ->
                val rawName = backStackEntry.arguments?.getString("name") ?: ""
                val categoryName = android.net.Uri.decode(rawName)
                CategoryDetailScreen(
                    categoryName = categoryName,
                    viewModel = dashboardViewModel,
                    onBack = { navController.popBackStack() },
                    onItemClick = { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    }
                )
            }

            // ── Settings ─────────────────────────────────────────────────────
            composable(Screen.Settings.route) {
                SettingsScreen(
                    userEmail = userEmail,
                    userName = userName,
                    userProfilePicture = userProfilePicture,
                    appVersion = com.receiptwarranty.app.BuildConfig.VERSION_NAME,
                    viewModel = settingsViewModel,
                    onSignOut = onSignOut,
                    onExportCSV = { homeViewModel.exportToCSV(context) },
                    onExportJSON = { showExportSheet = true },
                    onImport = { navController.navigate(Screen.Import.route) },
                    onNavigateToPrivacy = { navController.navigate(Screen.Privacy.route) },
                    onNavigateToAboutMe = { navController.navigate(Screen.AboutMe.route) },
                    onNavigateToArchive = { navController.navigate(Screen.Archive.route) },
                    onNavigateToAdvanced = { navController.navigate(Screen.AdvancedSettings.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AdvancedSettings.route) {
                AdvancedSettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Import ─────────────────────────────────────────────────────────
            composable(Screen.Import.route) {
                ImportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onImportComplete = { count ->
                        // Optionally refresh home data
                    }
                )
            }

            composable(Screen.Archive.route) {
                val archivedItems by homeViewModel.archivedItems.collectAsStateWithLifecycle()
                ArchiveScreen(
                    items = archivedItems,
                    onItemClick = { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Stats ────────────────────────────────────────────────────────
            composable(Screen.Stats.route) {
                StatsScreen(
                    viewModel = statsViewModel,
                    onNavigateToTimeline = { navController.navigate(Screen.Timeline.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Privacy.route) {
                PrivacyScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.AboutMe.route) {
                AboutMeScreen(onBack = { navController.popBackStack() })
            }


            // ── Add ───────────────────────────────────────────────────────────
            composable(
                route = Screen.Add.route + "?autoScan={autoScan}&autoGallery={autoGallery}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("autoScan") { 
                        type = NavType.BoolType
                        defaultValue = false
                    },
                    navArgument("autoGallery") { 
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val typeString = backStackEntry.arguments?.getString("type")?.uppercase() ?: "WARRANTY"
                val autoScan = backStackEntry.arguments?.getBoolean("autoScan") ?: false
                val autoGallery = backStackEntry.arguments?.getBoolean("autoGallery") ?: false
                
                val receiptType = try {
                    ReceiptType.valueOf(typeString)
                } catch (_: IllegalArgumentException) {
                    ReceiptType.WARRANTY
                }
                AddEditScreen(
                    item = null,
                    type = receiptType,
                    autoLaunchScanner = autoScan,
                    autoLaunchGallery = autoGallery,
                    tags = emptyList(),
                    categories = emptyList(),
                    viewModel = addEditViewModel,
                    onSave = { item ->
                        addEditViewModel.saveItem(item) {
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            // ── Edit ──────────────────────────────────────────────────────────
            composable(
                route = Screen.Edit.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                    ?: return@composable
                val itemFlow = detailViewModel.getItem(id)
                val itemState by itemFlow.collectAsStateWithLifecycle(initialValue = null)
                val currentItem = itemState ?: return@composable
                AddEditScreen(
                    item = currentItem,
                    type = currentItem.type,
                    tags = emptyList(),
                    categories = emptyList(),
                    viewModel = addEditViewModel,
                    onSave = { updated ->
                        addEditViewModel.saveItem(updated) {
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            // ── Detail (with deep-link support) ───────────────────────────────
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
                deepLinks = listOf(navDeepLink { uriPattern = "vaultapp://detail/{id}" })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                    ?: return@composable
                DetailScreen(
                    itemId = id,
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() },
                    onEdit = { item -> navController.navigate(Screen.Edit.createRoute(item.id)) }
                )
            }
        }
    }

    if (showAddTypeSheet) {
        TypeSelectionBottomSheet(
            onActionSelected = { action ->
                showAddTypeSheet = false
                when (action) {
                    com.receiptwarranty.app.ui.components.FabActionType.SCAN,
                    com.receiptwarranty.app.ui.components.FabActionType.GALLERY,
                    com.receiptwarranty.app.ui.components.FabActionType.MANUAL -> {
                        navController.navigate(Screen.Add.createRoute(ReceiptType.RECEIPT.name))
                    }
                    com.receiptwarranty.app.ui.components.FabActionType.BILL -> {
                        navController.navigate(Screen.Add.createRoute(ReceiptType.BILL.name))
                    }
                    com.receiptwarranty.app.ui.components.FabActionType.SUBSCRIPTION -> {
                        navController.navigate(Screen.Add.createRoute(ReceiptType.SUBSCRIPTION.name))
                    }
                }
            },
            onDismiss = { showAddTypeSheet = false }
        )
    }

    if (showExportSheet) {
        val exportState by dataManagementViewModel.exportState.collectAsStateWithLifecycle()
        
        com.receiptwarranty.app.ui.components.ExportOptionsBottomSheet(
            onExport = { category, dateRange, startDate, endDate, includeImages, destination ->
                dataManagementViewModel.exportData(
                    context = context,
                    category = category,
                    dateRange = dateRange,
                    customStartDate = startDate,
                    customEndDate = endDate,
                    includeImages = includeImages,
                    destination = destination
                )
            },
            onDismiss = {
                showExportSheet = false
                dataManagementViewModel.resetExportState()
            },
            isExporting = exportState.isExporting,
            exportProgress = exportState.progress
        )
        
        if (exportState.lastExportUri != null) {
            showExportSheet = false
            dataManagementViewModel.resetExportState()
        }
    }
}

