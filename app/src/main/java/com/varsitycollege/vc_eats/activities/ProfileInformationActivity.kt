package com.varsitycollege.vc_eats

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.vc_eats.databinding.ActivityProfileInformationBinding
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import com.varsitycollege.vc_eats.models.User
import kotlinx.coroutines.launch

class ProfileInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileInformationBinding
    private val firebaseManager = FirebaseManager.getInstance()
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadProfileData()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadProfileData() {
        val userId = firebaseManager.getCurrentUserId()

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val user = firebaseManager.getUser(userId)
            if (user != null) {
                currentUser = user
                binding.etFullName.setText(user.name)
                binding.etEmail.setText(user.email)
                binding.etStudentId.setText(user.id)

                // Load additional info from SharedPreferences if available
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                binding.etPhone.setText(sharedPref.getString("phone", ""))
                binding.etAddress.setText(sharedPref.getString("address", ""))
            } else {
                Toast.makeText(this@ProfileInformationActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveProfile.setOnClickListener {
            saveProfileData()
        }
    }

    private fun saveProfileData() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        // Validation
        if (fullName.isEmpty()) {
            binding.etFullName.error = "Full name is required"
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
            return
        }

        if (currentUser == null) {
            Toast.makeText(this, "User data not loaded", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            // Update user in Firebase
            val updatedUser = currentUser!!.copy(
                name = fullName,
                email = email
            )

            val success = firebaseManager.saveUser(updatedUser)

            if (success) {
                // Save additional info to SharedPreferences
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("user_name", fullName)
                    putString("user_email", email)
                    putString("phone", phone)
                    putString("address", address)
                    apply()
                }

                Toast.makeText(this@ProfileInformationActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this@ProfileInformationActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}