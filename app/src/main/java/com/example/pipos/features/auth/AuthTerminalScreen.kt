package com.example.pipos.features.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.pipos.core.components.TerminalButton
import com.example.pipos.core.components.TerminalButtonVariant
import com.example.pipos.core.components.TerminalPanel
import com.example.pipos.core.theme.BlinkingCursor
import com.example.pipos.core.theme.terminalCornerBrackets
import com.example.ui.theme.PipAmberWarning
import com.example.ui.theme.PipBorderGreen
import com.example.ui.theme.PipCriticalRed
import com.example.ui.theme.PipInfoCyan
import com.example.ui.theme.PipMonospace
import com.example.ui.theme.PipPanelBg
import com.example.ui.theme.PipPhosphorGreen
import com.example.ui.theme.PipPrimaryBg
import com.example.ui.theme.PipSecondaryBg
import com.example.ui.theme.PipSecondaryGreen
import com.example.ui.theme.PipTextDisabled
import com.example.ui.theme.PipTextMain
import com.example.ui.theme.PipTextSecondary
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthTerminalScreen(
    onAuthSuccess: () -> Unit,
    onContinueOffline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PipPrimaryBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PipPanelBg)
                .border(1.dp, PipBorderGreen)
                .terminalCornerBrackets(bracketColor = PipPhosphorGreen, bracketLength = 20f)
                .padding(20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "PIP-OS SECURITY GATEWAY",
                fontFamily = PipMonospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PipPhosphorGreen,
                letterSpacing = 1.sp
            )
            Text(
                text = "CLASSIFIED PROTOCOL ACCESS // CLEARANCE LEVEL 4",
                fontFamily = PipMonospace,
                fontSize = 10.sp,
                color = PipSecondaryGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PipSecondaryBg)
                    .border(1.dp, PipBorderGreen)
                    .padding(10.dp)
            ) {
                Text(
                    text = "► IDENTITY PROVIDER: GOOGLE IDENTITY",
                    fontFamily = PipMonospace,
                    fontSize = 11.sp,
                    color = PipPhosphorGreen
                )
                Text(
                    text = "► CLOUD DATABASE: FIREBASE FIRESTORE",
                    fontFamily = PipMonospace,
                    fontSize = 11.sp,
                    color = PipInfoCyan
                )
                Text(
                    text = "► STATUS: AWAITING OPERATOR SIGN-IN",
                    fontFamily = PipMonospace,
                    fontSize = 11.sp,
                    color = PipAmberWarning
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "► ENCRYPTION LINK: READY_",
                        fontFamily = PipMonospace,
                        fontSize = 11.sp,
                        color = PipTextSecondary
                    )
                    BlinkingCursor()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Authenticate with Google to enable real-time cloud synchronization, multi-device backup, and team workspace collaboration.",
                fontFamily = PipMonospace,
                fontSize = 11.sp,
                color = PipTextMain,
                lineHeight = 16.sp
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "! AUTH ERROR: $errorMessage",
                    fontFamily = PipMonospace,
                    fontSize = 11.sp,
                    color = PipCriticalRed,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google Sign-In Button
            TerminalButton(
                text = if (isLoading) "AUTHENTICATING..." else "SIGN IN WITH GOOGLE",
                onClick = {
                    isLoading = true
                    errorMessage = null
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isLoading = false
                            onAuthSuccess()
                        },
                        onAuthError = { err ->
                            isLoading = false
                            errorMessage = err
                        },
                        scope = coroutineScope,
                        onAuthCancelled = {
                            isLoading = false
                        }
                    )
                },
                enabled = !isLoading,
                variant = TerminalButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth(),
                testTag = "google_sign_in_btn"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Continue Offline Button
            TerminalButton(
                text = "PROCEED IN OFFLINE ISOLATED MODE",
                onClick = onContinueOffline,
                variant = TerminalButtonVariant.SECONDARY,
                modifier = Modifier.fillMaxWidth(),
                testTag = "continue_offline_btn"
            )
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type: ${credential.type}")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}
