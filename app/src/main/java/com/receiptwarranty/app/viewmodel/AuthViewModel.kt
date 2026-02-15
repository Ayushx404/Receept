package com.receiptwarranty.app.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.receiptwarranty.app.data.auth.GoogleAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        _authState.value = if (googleAuthManager.isSignedIn()) {
            AuthState.Authenticated(googleAuthManager.getCurrentUser()!!)
        } else {
            AuthState.Unauthenticated
        }
    }

    fun getSignInIntent(): Intent {
        return googleAuthManager.getSignInIntent()
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _isSigningIn.value = true
            try {
                val user = googleAuthManager.handleSignInResult(data)
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Error("Sign in failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    fun signOut() {
        googleAuthManager.signOut {
            _authState.value = AuthState.Unauthenticated
        }
    }

    class Factory(
        private val googleAuthManager: GoogleAuthManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(googleAuthManager) as T
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
