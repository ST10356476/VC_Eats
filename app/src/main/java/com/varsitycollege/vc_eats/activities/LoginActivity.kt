package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.vc_eats.databinding.ActivityLoginBinding
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import com.varsitycollege.vc_eats.viewmodels.LoginState
import com.varsitycollege.vc_eats.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    // Remember last credentials used for login
    private var lastEmail: String? = null
    private var lastPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        setupBiometricAuth()
        checkIfUserLoggedIn()
        setupClickListeners()
        observeLoginState()
    }

    private fun checkIfUserLoggedIn() {
        val userId = FirebaseManager.getInstance().getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                val user = FirebaseManager.getInstance().getUser(userId)
                val role = user?.role ?: "CUSTOMER"
                navigateBasedOnRole(role)
            }
        }
    }

    private fun setupClickListeners() {
        // Sign in
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                lastEmail = email
                lastPassword = password
                viewModel.signIn(email, password)
            }
        }

        // Sign up
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Google sign in placeholder
        binding.btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show()
        }

        // Biometric login
        binding.btnBiometric.setOnClickListener {
            if (BiometricPrefs.isBiometricEnabled(this)) {
                biometricPrompt.authenticate(promptInfo)
            } else {
                Toast.makeText(
                    this,
                    "Biometric login not set up yet. Please login with email and password first.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun observeLoginState() {
        // Result of login
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Success -> {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Save credentials for future biometric login
                        val email = lastEmail
                        val password = lastPassword
                        if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                            BiometricPrefs.saveCredentials(this@LoginActivity, email, password)
                            binding.btnBiometric.isEnabled = true
                            binding.btnBiometric.alpha = 1f
                        }

                        navigateBasedOnRole(state.userRole)
                    }

                    is LoginState.Error -> {
                        Toast.makeText(
                            this@LoginActivity,
                            state.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    LoginState.Initial -> {
                        // Nothing
                    }
                }
            }
        }

        // Loading state
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
            else -> Intent(this, CustomerMenuActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun validateInput(email: String, password: String): Boolean {
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        return when {
            email.isEmpty() -> {
                binding.tilEmail.error = "Email is required"
                false
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Please enter a valid email"
                false
            }

            password.isEmpty() -> {
                binding.tilPassword.error = "Password is required"
                false
            }

            password.length < 6 -> {
                binding.tilPassword.error = "Password must be at least 6 characters"
                false
            }

            else -> true
        }
    }

    private fun setupBiometricAuth() {
        val biometricManager = BiometricManager.from(this)

        val canAuthCode = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        val canUseBiometric = when (canAuthCode) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                true
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "No biometric hardware on this device", Toast.LENGTH_LONG).show()
                false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "Biometric hardware currently unavailable", Toast.LENGTH_LONG).show()
                false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                    this,
                    "No fingerprint or face enrolled. Set it up in device settings.",
                    Toast.LENGTH_LONG
                ).show()
                false
            }

            else -> {
                Toast.makeText(this, "Biometric not available", Toast.LENGTH_LONG).show()
                false
            }
        }

        val enabledInPrefs = BiometricPrefs.isBiometricEnabled(this)

        // Initial state of the button on screen load
        binding.btnBiometric.isEnabled = canUseBiometric && enabledInPrefs
        binding.btnBiometric.alpha = if (binding.btnBiometric.isEnabled) 1f else 0.5f

        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val email = BiometricPrefs.getEmail(this@LoginActivity)
                    val password = BiometricPrefs.getPassword(this@LoginActivity)

                    if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                        lastEmail = email
                        lastPassword = password
                        viewModel.signIn(email, password)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "No biometric credentials saved. Please login manually first.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        this@LoginActivity,
                        "Authentication error: $errString",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        this@LoginActivity,
                        "Authentication failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setSubtitle("Use your fingerprint or device credential to login")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }
}
