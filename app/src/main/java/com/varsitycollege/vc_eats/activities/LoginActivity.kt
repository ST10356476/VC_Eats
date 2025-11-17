package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.vc_eats.api.RetrofitClient
import com.varsitycollege.vc_eats.databinding.ActivityLoginBinding
import com.varsitycollege.vc_eats.models.LoginRequest
import com.varsitycollege.vc_eats.utils.TokenManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (validateInput(email, password)) loginUser(email, password)
        }

        binding.tvSignUp.setOnClickListener {
            Toast.makeText(this, "Sign up not implemented", Toast.LENGTH_SHORT).show()
        }
    }

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
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        return true
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        TokenManager.saveToken(body.data.token) // Use data.token
                        navigateBasedOnRole(body.data.user.role) // Use data.user.role
                    }
                    else {
                        Toast.makeText(this@LoginActivity, "Invalid response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateBasedOnRole(role: String) {
        val intent = when (role.uppercase()) {
            "STUDENT" -> Intent(this, CustomerMenuActivity::class.java)
            "STAFF", "ADMIN" -> Intent(this, StaffDashboardActivity::class.java)
            else -> Intent(this, CustomerMenuActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
