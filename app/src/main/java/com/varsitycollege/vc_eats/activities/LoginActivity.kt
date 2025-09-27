package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.vc_eats.viewmodels.LoginState
import com.varsitycollege.vc_eats.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Observe login state
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Success -> {
                        when (state.userRole) {
                            "CUSTOMER" -> {
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        CustomerMenuActivity::class.java
                                    )
                                )
                            }
                            "STAFF", "ADMIN" -> {
                                startActivity(Intent(this@LoginActivity, StaffDashboardActivity::class.java))
                            }
                        }
                        finish()
                    }
                    is LoginState.Error -> {
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    LoginState.Initial -> { /* Do nothing */ }
                }
            }
        }

        // Setup click listeners
        findViewById<Button>(R.id.btnSignIn).setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()
            viewModel.signIn(email, password)
        }
    }
}