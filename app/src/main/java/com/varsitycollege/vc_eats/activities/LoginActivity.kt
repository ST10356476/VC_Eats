package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.varsitycollege.vc_eats.viewmodels.LoginState
import com.varsitycollege.vc_eats.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Check if user is already logged in
        checkIfUserLoggedIn()

        setupClickListeners()
        observeLoginState()
    }

    private fun checkIfUserLoggedIn() {
        // If user is already logged in, navigate to appropriate screen
        // You can implement this later when Firebase is working
    }

    private fun setupClickListeners() {
        // Sign In Button
        findViewById<MaterialButton>(R.id.btnSignIn).setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString().trim()

            if (validateInput(email, password)) {
                viewModel.signIn(email, password)
            }
        }

        // Staff Login Button
        findViewById<MaterialButton>(R.id.btnStaffLogin).setOnClickListener {
            // For now, just navigate to staff dashboard
            // Later you can add staff-specific login
            startActivity(Intent(this, StaffDashboardActivity::class.java))
        }

        // Admin Login Button
        findViewById<MaterialButton>(R.id.btnAdminLogin).setOnClickListener {
            // For now, just navigate to staff dashboard
            // Later you can add admin-specific login
            startActivity(Intent(this, StaffDashboardActivity::class.java))
        }

        // Google Sign In Button (for later implementation)
        findViewById<MaterialButton>(R.id.btnGoogle).setOnClickListener {
            Toast.makeText(this, "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Biometric Button (for later implementation)
        findViewById<MaterialButton>(R.id.btnBiometric).setOnClickListener {
            Toast.makeText(this, "Biometric login coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Success -> {
                        navigateBasedOnRole(state.userRole)
                    }
                    is LoginState.Error -> {
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    LoginState.Initial -> {
                        // Do nothing
                    }
                }
            }
        }

        // Observe loading state to show/hide progress
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // You can show/hide a progress bar here
                findViewById<MaterialButton>(R.id.btnSignIn).isEnabled = !isLoading
                findViewById<MaterialButton>(R.id.btnSignIn).text = if (isLoading) "Signing In..." else "Sign In"
            }
        }
    }

    private fun navigateBasedOnRole(userRole: String) {
        val intent = when (userRole) {
            "CUSTOMER" -> Intent(this, CustomerMenuActivity::class.java)
            "STAFF", "ADMIN" -> Intent(this, StaffDashboardActivity::class.java)
            else -> Intent(this, CustomerMenuActivity::class.java) // Default to customer
        }

        startActivity(intent)
        finish() // Close login activity
    }

    private fun validateInput(email: String, password: String): Boolean {
        when {
            email.isEmpty() -> {
                findViewById<TextInputEditText>(R.id.etEmail).error = "Email is required"
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                findViewById<TextInputEditText>(R.id.etEmail).error = "Please enter a valid email"
                return false
            }
            password.isEmpty() -> {
                findViewById<TextInputEditText>(R.id.etPassword).error = "Password is required"
                return false
            }
            password.length < 6 -> {
                findViewById<TextInputEditText>(R.id.etPassword).error = "Password must be at least 6 characters"
                return false
            }
            else -> return true
        }
    }
}