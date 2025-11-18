package com.varsitycollege.vc_eats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varsitycollege.vc_eats.databinding.ActivitySettingsBinding
import com.varsitycollege.vc_eats.utils.LocaleHelper
import com.varsitycollege.vc_eats.utils.TokenManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var firebaseAuth: FirebaseAuth  // Firebase Authentication instance
    private lateinit var firestore: FirebaseFirestore  // Firebase Firestore instance

    // Attach base context to apply the selected language using LocaleHelper
    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupToolbar()         // Setup top toolbar
        loadUserFromFirebase() // Load user data from Firebase
        setupClickListeners()  // Setup all button/switch click listeners
        loadUserPreferences()  // Load local user preferences (switch states, language)
    }

    /**
     * Sets up toolbar with back button and title
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    /**
     * Load user info from Firebase Firestore
     * If user is not logged in, navigate to login
     */
    private fun loadUserFromFirebase() {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }

        // Fetch user document from "users" collection
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val email = doc.getString("email") ?: "N/A"
                    val role = doc.getString("role") ?: "N/A"
                    val userId = uid

                    // Display user info on UI
                    setupUserProfile(email, email, userId)
                } else {
                    // Document does not exist
                    Toast.makeText(this, getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }
            }
            .addOnFailureListener {
                // Firestore fetch failed
                Toast.makeText(this, "Failed to load user profile", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
    }

    /**
     * Sets the user's profile info (name, email, user ID, initials) on the UI
     */
    private fun setupUserProfile(userName: String, userEmail: String, userId: String) {
        binding.tvUserName.text = userName
        binding.tvUserEmail.text = userEmail
        binding.chipStudentId.text = "User ID: ${userId.take(10)}..." // Display first 10 chars of UID

        // Generate initials from userName
        val initials = userName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()

        binding.tvUserInitials.text = initials
    }

    /**
     * Sets up click listeners for profile info, switches, language card, and sign out
     */
    private fun setupClickListeners() {
        // Open Profile Information screen
        binding.cardProfileInfo.setOnClickListener {
            val intent = Intent(this, ProfileInformationActivity::class.java)
            startActivity(intent)
        }

        // Biometric switch toggle
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("biometric_enabled", isChecked)
            val message = if (isChecked) getString(R.string.biometric_enabled) else getString(R.string.biometric_disabled)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // Other switches for order updates, promotions, daily specials, push notifications
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

        // Open language selection dialog
        binding.cardLanguage.setOnClickListener {
            showLanguageDialog()
        }

        // Offline mode toggle
        binding.switchOfflineMode.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference("offline_mode", isChecked)
            val message = if (isChecked) getString(R.string.offline_mode_enabled) else getString(R.string.offline_mode_disabled)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // Sign out button
        binding.btnSignOut.setOnClickListener {
            showSignOutDialog()
        }
    }

    /**
     * Load saved switch states and language from SharedPreferences
     */
    private fun loadUserPreferences() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        binding.switchBiometric.isChecked = sharedPref.getBoolean("biometric_enabled", false)
        binding.switchOrderUpdates.isChecked = sharedPref.getBoolean("order_updates", true)
        binding.switchPromotions.isChecked = sharedPref.getBoolean("promotions", true)
        binding.switchDailySpecials.isChecked = sharedPref.getBoolean("daily_specials", false)
        binding.switchPushNotifications.isChecked = sharedPref.getBoolean("push_notifications", true)
        binding.switchOfflineMode.isChecked = sharedPref.getBoolean("offline_mode", false)

        val languageCode = LocaleHelper.getLanguage(this)
        binding.tvSelectedLanguage.text = LocaleHelper.getLanguageName(languageCode)
    }

    /**
     * Save boolean preference to SharedPreferences
     */
    private fun saveBooleanPreference(key: String, value: Boolean) {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    /**
     * Show language selection dialog
     */
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

                LocaleHelper.setLocale(this, languageCode)

                Toast.makeText(
                    this,
                    getString(R.string.language_changed, selectedLanguage),
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
                recreateActivity() // Recreate activity to apply new language
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun recreateActivity() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    /**
     * Show confirmation dialog before signing out
     */
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

    /**
     * Sign out user:
     * - Clear API token
     * - Sign out from Firebase
     * - Clear SharedPreferences
     * - Navigate to login screen
     */
    private fun signOut() {
        TokenManager.clearToken()     // Clear API token
        firebaseAuth.signOut()         // Sign out from Firebase

        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }

        navigateToLogin()
    }

    /**
     * Navigate user to login activity and clear backstack
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
