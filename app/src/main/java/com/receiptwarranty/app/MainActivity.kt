package com.receiptwarranty.app

import android.Manifest
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.viewModels
import com.receiptwarranty.app.ui.ReceiptWarrantyNavHost
import com.receiptwarranty.app.ui.screens.LoginScreen
import com.receiptwarranty.app.ui.theme.ReceiptWarrantyTheme
import com.receiptwarranty.app.viewmodel.AuthState
import com.receiptwarranty.app.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize auth manager
        (application as ReceiptWarrantyApp).container.initializeAuth(this)

        enableEdgeToEdge()
        setContent {
            val container = (application as ReceiptWarrantyApp).container
            val authManager = container.googleAuthManager

            if (authManager != null) {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(authManager)
                )

                val authState by authViewModel.authState.collectAsState()
                val isSigningIn by authViewModel.isSigningIn.collectAsState()

                // Create launcher for Google Sign-In
                val signInLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    authViewModel.handleSignInResult(result.data)
                }

                ReceiptWarrantyTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (val state = authState) {
                            is AuthState.Loading -> {
                                LoginScreen(
                                    onSignInClick = {
                                        val intent = authViewModel.getSignInIntent()
                                        signInLauncher.launch(intent)
                                    },
                                    isLoading = isSigningIn
                                )
                            }
                            is AuthState.Unauthenticated -> {
                                LoginScreen(
                                    onSignInClick = {
                                        val intent = authViewModel.getSignInIntent()
                                        signInLauncher.launch(intent)
                                    },
                                    isLoading = isSigningIn,
                                    errorMessage = null
                                )
                            }
                            is AuthState.Authenticated -> {
                                // Initialize Firestore with user ID
                                container.initializeFirestore(state.user.uid)
                                
                                PermissionHandler {
                                    ReceiptWarrantyNavHost(
                                        userId = state.user.uid,
                                        userEmail = state.user.email ?: "",
                                        onSignOut = { 
                                            container.firestoreRepository = null
                                            container.syncManager = null
                                            container.currentUserId = null
                                            authViewModel.signOut() 
                                        }
                                    )
                                }
                            }
                            is AuthState.Error -> {
                                LoginScreen(
                                    onSignInClick = {
                                        val intent = authViewModel.getSignInIntent()
                                        signInLauncher.launch(intent)
                                    },
                                    isLoading = isSigningIn,
                                    errorMessage = state.message
                                )
                            }
                        }
                    }
                }
            } else {
                // Fallback if auth not initialized
                ReceiptWarrantyTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        PermissionHandler {
                            ReceiptWarrantyNavHost(
                                userId = "local",
                                userEmail = "",
                                onSignOut = {}
                            )
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        }
    }
    content()
}
