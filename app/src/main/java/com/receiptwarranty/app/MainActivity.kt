package com.receiptwarranty.app

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.receiptwarranty.app.ui.ReceiptWarrantyNavHost
import com.receiptwarranty.app.ui.theme.ReceiptWarrantyTheme
import com.receiptwarranty.app.viewmodel.AuthState
import com.receiptwarranty.app.ui.screens.LoginScreen
import com.receiptwarranty.app.viewmodel.AuthViewModel
import androidx.core.content.edit
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import javax.inject.Inject
import com.receiptwarranty.app.data.AppearancePreferences

private object PreferenceKeys {
    const val APP_PREFS = "app_prefs"
    const val IS_LOCAL_MODE = "is_local_mode"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appearancePreferences: AppearancePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val prefs = getSharedPreferences(PreferenceKeys.APP_PREFS, Context.MODE_PRIVATE)
            var isLocalMode by remember { mutableStateOf(prefs.getBoolean(PreferenceKeys.IS_LOCAL_MODE, false)) }

            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsStateWithLifecycle()
            val isSigningIn by authViewModel.isSigningIn.collectAsStateWithLifecycle()

            // collect appearance settings
            val appearanceSettings by appearancePreferences.settings.collectAsStateWithLifecycle()

            // Create launcher for Google Sign-In
            val signInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                authViewModel.handleSignInResult(result.data)
            }

                ReceiptWarrantyTheme(
                    themeMode = appearanceSettings.themeMode,
                    primaryColor = appearanceSettings.primaryColorHex,
                    useDynamicColor = appearanceSettings.useDynamicColor,
                    useAmoledBlack = appearanceSettings.useAmoledBlack
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (isLocalMode) {
                            PermissionHandler {
                                ReceiptWarrantyNavHost(
                                    userId = "local",
                                    userEmail = "",
                                    onSignOut = {
                                        isLocalMode = false
                                        prefs.edit { putBoolean(PreferenceKeys.IS_LOCAL_MODE, false) }
                                    }
                                )
                            }
                        } else {
                            when (val state = authState) {
                                is AuthState.Authenticated -> {
                                    PermissionHandler {
                                        ReceiptWarrantyNavHost(
                                            userId = state.user.uid,
                                            userEmail = state.user.email ?: "",
                                            userName = state.user.displayName,
                                            userProfilePicture = state.user.photoUrl?.toString(),
                                            onSignOut = { 
                                                authViewModel.signOut()
                                                isLocalMode = false
                                                prefs.edit { putBoolean(PreferenceKeys.IS_LOCAL_MODE, false) }
                                            }
                                        )
                                    }
                                }
                                else -> {
                                    LoginScreen(
                                        onSignInClick = {
                                            signInLauncher.launch(authViewModel.getSignInIntent())
                                        },
                                        onSkipLogin = {
                                            isLocalMode = true
                                            prefs.edit { putBoolean(PreferenceKeys.IS_LOCAL_MODE, true) }
                                        },
                                        isLoading = isSigningIn,
                                        errorMessage = (state as? AuthState.Error)?.message
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }
}

@Composable
private fun PermissionHandler(content: @Composable () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            }
            else -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (permissions.isNotEmpty()) {
            launcher.launch(permissions)
        }
    }
    content()
}



