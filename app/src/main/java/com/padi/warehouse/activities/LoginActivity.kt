package com.padi.warehouse.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.padi.warehouse.BuildConfig
import com.padi.warehouse.R
import com.padi.warehouse.user


// There is a strange bug and Firebase Auth UI is working only in release variant.
// In debug variant it is not calling the onSignInResult callback and keep showing the logging
// screen. You have to close completely and reopen the app in order to be signed in.
class LoginActivity : AppCompatActivity() {
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        if (user == null) {
            // Choose authentication providers
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build()
            )

            // Create and launch sign-in intent
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.AppTheme)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
                .setLogo(R.drawable.ic_stock)      // Set logo drawable
                .build()
            signInLauncher.launch(signInIntent)
        } else {
            // Successfully signed in
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            user = FirebaseAuth.getInstance().currentUser
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            Toast.makeText(
                this, response?.error?.toString(),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
