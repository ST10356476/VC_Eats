package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varsitycollege.vc_eats.api.RetrofitClient
import com.varsitycollege.vc_eats.databinding.ActivityLoginBinding
import com.varsitycollege.vc_eats.models.LoginRequest
import com.varsitycollege.vc_eats.utils.TokenManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize API token manager
        TokenManager.init(this)

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inflate layout with ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Login button click
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password) // Attempt API login
            }
        }

        // Sign up text click (not implemented)
        binding.tvSignUp.setOnClickListener {
            Toast.makeText(this, "Sign up not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Validate email & password fields
     */
    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email"
            return false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password required"
            return false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be 6+ chars"
            return false
        }

        // Clear errors if valid
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        return true
    }

    /**
     * Attempt login with API first
     */
    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success) {

                        // Save API token
                        TokenManager.saveToken(body.data.token)

                        // Sync user with Firebase
                        firebaseSignIn(email, password, body.data.user.role)

                    } else {
                        Toast.makeText(this@LoginActivity, body?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@LoginActivity, "Login failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Hybrid Firebase sign-in
     * - Try to sign in existing user
     * - If fails, create a new Firebase account
     */
    private fun firebaseSignIn(email: String, password: String, role: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                saveUserToFirestore(email, role) // Sync user data
            }
            .addOnFailureListener {
                // Firebase user doesn't exist, create new
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        saveUserToFirestore(email, role)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Firebase Auth Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
    }

    /**
     * Save user info to Firestore
     * Includes email, role, and API token
     */
    private fun saveUserToFirestore(email: String, role: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return

        val userMap = hashMapOf(
            "email" to email,
            "role" to role,
            "token" to TokenManager.getToken() // Store API token
        )

        firestore.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                navigateBasedOnRole(role) // Go to the correct dashboard
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to store user to Firebase", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Navigate based on user role
     */
    private fun navigateBasedOnRole(role: String) {
        val intent = when (role.uppercase()) {
            "STUDENT" -> Intent(this, CustomerMenuActivity::class.java)
            "STAFF", "ADMIN" -> Intent(this, StaffDashboardActivity::class.java)
            else -> Intent(this, CustomerMenuActivity::class.java)
        }

        // Clear backstack to prevent returning to login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
