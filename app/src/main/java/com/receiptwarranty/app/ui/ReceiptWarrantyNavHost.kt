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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.receiptwarranty.app.data.ReceiptType
import com.receiptwarranty.app.ui.screens.AddEditScreen
import com.receiptwarranty.app.ui.screens.DashboardScreen
import com.receiptwarranty.app.ui.screens.DetailScreen
import com.receiptwarranty.app.ui.screens.HomeScreen
import com.receiptwarranty.app.ui.screens.SettingsScreen
import com.receiptwarranty.app.ui.components.TypeSelectionBottomSheet
import com.receiptwarranty.app.viewmodel.ReceiptWarrantyViewModel

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

@Composable
fun ReceiptWarrantyNavHost(
    navController: NavHostController = rememberNavController(),
    userId: String = "local",
    userEmail: String = "",
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val appContainer = remember { 
        (context.applicationContext as com.receiptwarranty.app.ReceiptWarrantyApp).container 
    }
    val viewModel: ReceiptWarrantyViewModel = viewModel(
        factory = ReceiptWarrantyViewModel.Factory(
            appContainer.repository,
            appContainer.warrantyReminderScheduler,
            appContainer.syncStateManager
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    var showAddTypeSheet by remember { mutableStateOf(false) }
    
    // Selection state for multi-select delete
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf(setOf<Long>()) }
    
    // Helper functions for selection
    fun enterSelectionMode(item: com.receiptwarranty.app.data.ReceiptWarranty) {
        isSelectionMode = true
        selectedItemIds = setOf(item.id)
    }
    
    fun toggleSelection(itemId: Long) {
        selectedItemIds = if (selectedItemIds.contains(itemId)) {
            selectedItemIds - itemId
        } else {
            selectedItemIds + itemId
        }
        // Exit selection mode if nothing selected
        if (selectedItemIds.isEmpty()) {
            isSelectionMode = false
        }
    }
    
    fun exitSelectionMode() {
        isSelectionMode = false
        selectedItemIds = emptySet()
    }
    
    fun deleteSelectedItems() {
        val selectedItems = uiState.items.filter { selectedItemIds.contains(it.id) }
        viewModel.deleteMultiple(selectedItems)
        exitSelectionMode()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = navController.currentDestination?.route == Screen.Home.route,
                    onClick = {
                        if (navController.currentDestination?.route != Screen.Home.route) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = navController.currentDestination?.route == Screen.Dashboard.route,
                    onClick = {
                        if (navController.currentDestination?.route != Screen.Dashboard.route) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = navController.currentDestination?.route == Screen.Settings.route,
                    onClick = {
                        if (navController.currentDestination?.route != Screen.Settings.route) {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTypeSheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    items = uiState.items,
                    searchQuery = uiState.searchQuery,
                    warrantyFilter = uiState.warrantyFilter,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onWarrantyFilterChange = viewModel::setWarrantyFilter,
                    onItemClick = { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    },
                    onAddClick = { showAddTypeSheet = true },
                    isSelectionMode = isSelectionMode,
                    selectedItems = selectedItemIds,
                    onEnterSelectionMode = { item -> enterSelectionMode(item) },
                    onSelectItem = { itemId -> toggleSelection(itemId) },
                    onExitSelectionMode = { exitSelectionMode() },
                    onDeleteSelected = { deleteSelectedItems() }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    uiState = uiState,
                    onNavigateToAdd = { type ->
                        showAddTypeSheet = false
                        navController.navigate(Screen.Add.createRoute(type.name))
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToCategory = { },
                    onNavigateToExpiringSoon = { },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onExportCSV = { viewModel.exportToCSV(context) },
                    viewModel = viewModel
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    userEmail = userEmail,
                    syncStateManager = appContainer.syncStateManager,
                    onSignOut = onSignOut,
                    onExportCSV = { viewModel.exportToCSV(context) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Add.route,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val typeString = backStackEntry.arguments?.getString("type")?.uppercase() ?: "WARRANTY"
                val type = try {
                    ReceiptType.valueOf(typeString)
                } catch (e: IllegalArgumentException) {
                    ReceiptType.WARRANTY
                }

                AddEditScreen(
                    item = null,
                    type = type,
                    companies = uiState.companies,
                    categories = uiState.categories,
                    onSave = { item ->
                        viewModel.insertOrUpdate(item)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(Screen.Edit.route) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable

                val item by appContainer.receiptWarrantyDao.getById(id).collectAsState(initial = null)
                val currentItem = item

                // Don't pop when null - just return and let the loading state show
                // This fixes the issue where the screen would flash and close

                AddEditScreen(
                    item = currentItem,
                    type = currentItem?.type,
                    companies = uiState.companies,
                    categories = uiState.categories,
                    onSave = { updated ->
                        viewModel.insertOrUpdate(updated)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(Screen.Detail.route) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable

                val item by appContainer.receiptWarrantyDao.getById(id).collectAsState(initial = null)
                val currentItem = item

                // Don't pop when null - just return and let the loading state show
                // This fixes the issue where the screen would flash and close

                DetailScreen(
                    item = currentItem,
                    onEdit = { navController.navigate(Screen.Edit.createRoute(id)) },
                    onDelete = {
                        viewModel.deleteById(id)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showAddTypeSheet) {
        TypeSelectionBottomSheet(
            onTypeSelected = { type ->
                showAddTypeSheet = false
                navController.navigate(Screen.Add.createRoute(type.name))
            },
            onDismiss = { showAddTypeSheet = false }
        )
    }
}
