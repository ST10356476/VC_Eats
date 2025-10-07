package com.varsitycollege.vc_eats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.vc_eats.databinding.ActivitySettingsBinding
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import com.varsitycollege.vc_eats.utils.LocaleHelper
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val firebaseManager = FirebaseManager.getInstance()

    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

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
        supportActionBar?.title = getString(R.string.settings)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadUserFromFirebase() {
        val userId = firebaseManager.getCurrentUserId()

        if (userId == null) {
            Toast.makeText(this, getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }

        lifecycleScope.launch {
            val user = firebaseManager.getUser(userId)
            if (user != null) {
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("user_id", user.id)
                    putString("user_name", user.name)
                    putString("user_email", user.email)
                    putString("user_role", user.role)
                    putString("profile_image_url", user.profileImageUrl)
                    apply()
                }

                setupUserProfile(user.name, user.email, userId)
            } else {
                Toast.makeText(this@SettingsActivity, getString(R.string.failed_load_user_data), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUserProfile(userName: String, userEmail: String, userId: String) {
        binding.tvUserName.text = userName
        binding.tvUserEmail.text = userEmail
        binding.chipStudentId.text = "User ID: ${userId.take(10)}..."

        val initials = userName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
        binding.tvUserInitials.text = initials
    }

    private fun setupClickListeners() {
        binding.cardProfileInfo.setOnClickListener {
            val intent = Intent(this, ProfileInformationActivity::class.java)
            startActivityForResult(intent, PROFILE_UPDATE_REQUEST)
        }

        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("biometric_enabled", isChecked)
            val message = if (isChecked) getString(R.string.biometric_enabled) else getString(R.string.biometric_disabled)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        binding.switchOrderUpdates.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("order_updates", isChecked)
        }

        binding.switchPromotions.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("promotions", isChecked)
        }

        binding.switchDailySpecials.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("daily_specials", isChecked)
        }

        binding.switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("push_notifications", isChecked)
        }

        binding.cardLanguage.setOnClickListener {
            showLanguageDialog()
        }

        binding.switchOfflineMode.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("offline_mode", isChecked)
            val message = if (isChecked) getString(R.string.offline_mode_enabled) else getString(R.string.offline_mode_disabled)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

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

        val languageCode = LocaleHelper.getLanguage(this)
        val languageName = LocaleHelper.getLanguageName(languageCode)
        binding.tvSelectedLanguage.text = languageName
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
        val currentLanguageCode = LocaleHelper.getLanguage(this)
        val currentLanguage = LocaleHelper.getLanguageName(currentLanguageCode)
        val checkedItem = languages.indexOf(currentLanguage)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val selectedLanguage = languages[which]
                val languageCode = LocaleHelper.getLanguageCode(selectedLanguage)

                // Save language preference
                LocaleHelper.setLocale(this, languageCode)

                // Show toast with selected language
                Toast.makeText(
                    this,
                    getString(R.string.language_changed, selectedLanguage),
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()

                // Restart activity to apply language change
                recreateActivity()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun recreateActivity() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.sign_out_title))
            .setMessage(getString(R.string.sign_out_message))
            .setPositiveButton(getString(R.string.sign_out)) { _, _ ->
                signOut()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun signOut() {
        firebaseManager.signOut()

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