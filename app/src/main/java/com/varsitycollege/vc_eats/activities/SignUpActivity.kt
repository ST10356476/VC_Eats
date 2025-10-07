package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.vc_eats.databinding.ActivitySignUpBinding
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Sign Up Button
        binding.btnSignUp.setOnClickListener {
            validateAndSignUp()
        }

        // Google Sign Up Button
        binding.btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google Sign Up coming soon", Toast.LENGTH_SHORT).show()
        }

        // Sign In Link
        binding.tvSignIn.setOnClickListener {
            finish() // Go back to login activity
        }
    }

    private fun validateAndSignUp() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val termsAccepted = binding.cbTerms.isChecked

        // Clear previous errors
        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        // Validation
        var isValid = true

        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Full name is required"
            isValid = false
        } else if (fullName.length < 3) {
            binding.tilFullName.error = "Name must be at least 3 characters"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        if (!termsAccepted) {
            Toast.makeText(this, "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!isValid) return

        // Proceed with sign up
        signUpUser(fullName, email, password)
    }

    private fun signUpUser(fullName: String, email: String, password: String) {
        // Show loading state
        binding.btnSignUp.isEnabled = false
        binding.btnSignUp.text = "Creating Account..."

        lifecycleScope.launch {
            val success = firebaseManager.signUp(email, password, fullName)

            if (success) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Account created successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                // Navigate to main activity or login
                val intent = Intent(this@SignUpActivity, CustomerMenuActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this@SignUpActivity,
                    "Sign up failed. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()

                // Reset button
                binding.btnSignUp.isEnabled = true
                binding.btnSignUp.text = "Create Account"
            }
        }
    }
}