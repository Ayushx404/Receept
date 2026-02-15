package com.receiptwarranty.app.data.auth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class GoogleAuthManager(private val activity: Activity) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val webClientId: String by lazy {
        activity.getString(com.receiptwarranty.app.R.string.default_web_client_id)
    }

    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .requestServerAuthCode(webClientId)
            .requestScopes(
                Scope(DriveScopes.DRIVE_FILE),
                Scope(DriveScopes.DRIVE_APPDATA)
            )
            .build()
    }

    val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(activity, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun handleSignInResult(data: Intent?): FirebaseUser? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isSignedIn(): Boolean = auth.currentUser != null

    fun signOut(onComplete: () -> Unit) {
        auth.signOut()
        googleSignInClient.signOut()
            .addOnCompleteListener { onComplete() }
    }

    suspend fun getAccessToken(): String? {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(activity)
            account?.idToken
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getDriveAccessToken(): String? {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(activity)
            account?.serverAuthCode
            account?.let {
                // Request access token specifically for Drive API
                val scope = "https://www.googleapis.com/auth/drive.file"
                null // We'll handle this differently - see DriveStorageManager
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getGoogleAccount() = GoogleSignIn.getLastSignedInAccount(activity)
}
