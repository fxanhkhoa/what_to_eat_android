package com.fxanhkhoa.what_to_eat_android.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class GoogleSignInHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleSignInHelper"
        // Your actual client ID from the Google OAuth configuration
        private const val WEB_CLIENT_ID = "500870159993-95slkr86hsjh4i07tgosrk1bulgjavvr.apps.googleusercontent.com"
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>): GoogleSignInResult {
        return try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {
                Log.d(TAG, "Sign-in successful, idToken obtained")
                Log.d(TAG, "User email: ${account.email}")
                Log.d(TAG, "User name: ${account.displayName}")
                GoogleSignInResult.Success(
                    idToken = idToken,
                    email = account.email,
                    displayName = account.displayName,
                    photoUrl = account.photoUrl?.toString()
                )
            } else {
                Log.e(TAG, "ID token is null")
                GoogleSignInResult.Error("Failed to get ID token")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Sign-in failed with status code: ${e.statusCode}")
            val errorMessage = when (e.statusCode) {
                10 -> "DEVELOPER_ERROR: Check SHA-1 fingerprints in Google Console. Current debug SHA-1: E8:AF:F5:70:7A:87:4D:4A:96:4F:08:03:09:F6:84:34:98:9A:77:EC"
                12501 -> "CANCELLED: User cancelled the sign-in"
                7 -> "NETWORK_ERROR: Check your internet connection"
                8 -> "INTERNAL_ERROR: Google Play Services internal error"
                else -> "Sign-in failed with code ${e.statusCode}: ${e.message}"
            }
            Log.e(TAG, errorMessage)
            GoogleSignInResult.Error(errorMessage)
        }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }

    fun revokeAccess(onComplete: () -> Unit = {}) {
        googleSignInClient.revokeAccess().addOnCompleteListener {
            onComplete()
        }
    }
}

sealed class GoogleSignInResult {
    data class Success(
        val idToken: String,
        val email: String?,
        val displayName: String?,
        val photoUrl: String?
    ) : GoogleSignInResult()

    data class Error(val message: String) : GoogleSignInResult()
    object Cancelled : GoogleSignInResult()
}

@Composable
fun rememberGoogleSignInHelper(): GoogleSignInHelper {
    val context = LocalContext.current
    return remember { GoogleSignInHelper(context) }
}
