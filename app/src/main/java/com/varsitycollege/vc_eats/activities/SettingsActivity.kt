package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.vc_eats.databinding.ActivitySettingsBinding
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadUserFromFirebase()
        setupClickListeners()
        loadUserPreferences()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadUserFromFirebase() {
        val userId = firebaseManager.getCurrentUserId()

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }

        lifecycleScope.launch {
            val user = firebaseManager.getUser(userId)
            if (user != null) {
                // Save to SharedPreferences for offline access
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("user_id", user.id)
                    putString("user_name", user.name)
                    putString("user_email", user.email)
                    putString("user_role", user.role)
                    putString("profile_image_url", user.profileImageUrl)
                    apply()
                }

                // Update UI
                setupUserProfile(user.name, user.email, userId)
            } else {
                Toast.makeText(this@SettingsActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUserProfile(userName: String, userEmail: String, userId: String) {
        binding.tvUserName.text = userName
        binding.tvUserEmail.text = userEmail
        binding.chipStudentId.text = "User ID: ${userId.take(10)}..."

        // Set initials for avatar
        val initials = userName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
        binding.tvUserInitials.text = initials
    }

    private fun setupClickListeners() {
        // Profile Information
        binding.cardProfileInfo.setOnClickListener {
            val intent = Intent(this, ProfileInformationActivity::class.java)
            startActivityForResult(intent, PROFILE_UPDATE_REQUEST)
        }

        // Biometric Authentication Switch
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("biometric_enabled", isChecked)
            if (isChecked) {
                Toast.makeText(this, "Biometric authentication enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Biometric authentication disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Order Updates Switch
        binding.switchOrderUpdates.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("order_updates", isChecked)
        }

        // Promotions & Offers Switch
        binding.switchPromotions.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("promotions", isChecked)
        }

        // Daily Specials Switch
        binding.switchDailySpecials.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("daily_specials", isChecked)
        }

        // Push Notifications Switch
        binding.switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("push_notifications", isChecked)
        }

        // Language Selector
        binding.cardLanguage.setOnClickListener {
            showLanguageDialog()
        }

        // Offline Mode Switch
        binding.switchOfflineMode.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("offline_mode", isChecked)
            if (isChecked) {
                Toast.makeText(this, "Offline mode enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Offline mode disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Sign Out Button
        binding.btnSignOut.setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun loadUserPreferences() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        binding.switchBiometric.isChecked = sharedPref.getBoolean("biometric_enabled", false)
        binding.switchOrderUpdates.isChecked = sharedPref.getBoolean("order_updates", true)
        binding.switchPromotions.isChecked = sharedPref.getBoolean("promotions", true)
        binding.switchDailySpecials.isChecked = sharedPref.getBoolean("daily_specials", false)
        binding.switchPushNotifications.isChecked = sharedPref.getBoolean("push_notifications", true)
        binding.switchOfflineMode.isChecked = sharedPref.getBoolean("offline_mode", false)

        val language = sharedPref.getString("language", "English") ?: "English"
        binding.tvSelectedLanguage.text = language
    }

    private fun saveBooleanPreference(key: String, value: Boolean) {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Afrikaans", "Zulu", "Xhosa")
        val currentLanguage = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getString("language", "English") ?: "English"
        val checkedItem = languages.indexOf(currentLanguage)

        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val selectedLanguage = languages[which]
                binding.tvSelectedLanguage.text = selectedLanguage

                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("language", selectedLanguage)
                    apply()
                }

                Toast.makeText(this, "Language changed to $selectedLanguage", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                signOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun signOut() {
        // Clear Firebase session
        firebaseManager.signOut()

        // Clear SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }

        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROFILE_UPDATE_REQUEST && resultCode == RESULT_OK) {
            loadUserFromFirebase()
        }
    }

    companion object {
        private const val PROFILE_UPDATE_REQUEST = 1001
    }
}