package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.varsitycollege.vc_eats.databinding.ActivityLoginBinding
import com.varsitycollege.vc_eats.viewmodels.LoginState
import com.varsitycollege.vc_eats.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                viewModel.signIn(email, password)
            }
        }

        // Sign Up Link
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Staff Login Button
        binding.btnStaffLogin.setOnClickListener {
            // For now, just navigate to staff dashboard
            // Later you can add staff-specific login
            startActivity(Intent(this, StaffDashboardActivity::class.java))
        }

        // Admin Login Button
        binding.btnAdminLogin.setOnClickListener {
            // For now, just navigate to staff dashboard
            // Later you can add admin-specific login
            startActivity(Intent(this, StaffDashboardActivity::class.java))
        }

        // Google Sign In Button (for later implementation)
        binding.btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Biometric Button (for later implementation)
        binding.btnBiometric.setOnClickListener {
            Toast.makeText(this, "Biometric login coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Success -> {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                        ).show()
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
                binding.btnSignIn.isEnabled = !isLoading
                binding.btnSignIn.text = if (isLoading) "Signing In..." else "Sign In"
            }
        }
    }

    private fun navigateBasedOnRole(userRole: String) {
        val intent = when (userRole) {
            "CUSTOMER" -> Intent(this, CustomerMenuActivity::class.java)
            "STAFF", "ADMIN" -> Intent(this, StaffDashboardActivity::class.java)
            else -> Intent(this, CustomerMenuActivity::class.java) // Default to customer
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close login activity
    }

    private fun validateInput(email: String, password: String): Boolean {
        // Clear previous errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        when {
            email.isEmpty() -> {
                binding.tilEmail.error = "Email is required"
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Please enter a valid email"
                return false
            }
            password.isEmpty() -> {
                binding.tilPassword.error = "Password is required"
                return false
            }
            password.length < 6 -> {
                binding.tilPassword.error = "Password must be at least 6 characters"
                return false
            }
            else -> return true
        }
    }
}