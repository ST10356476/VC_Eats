package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.varsitycollege.vc_eats.databinding.ActivityStaffDashboardBinding
import com.varsitycollege.vc_eats.firebase.FirebaseManager

class StaffDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffDashboardBinding
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Menu Management
        binding.cardMenuManagement.setOnClickListener {
            startActivity(Intent(this, MenuManagementActivity::class.java))
        }

        // Order Dashboard
        binding.cardOrderDashboard.setOnClickListener {
            startActivity(Intent(this, OrderDashboardActivity::class.java))
        }

        // Analytics
        binding.cardAnalytics.setOnClickListener {
            startActivity(Intent(this, AnalyticsReportsActivity::class.java))
        }

        // View All Orders
        binding.btnViewAllOrders.setOnClickListener {
            startActivity(Intent(this, OrderDashboardActivity::class.java))
        }

        // Sign Out Button
        binding.btnStaffSignOut.setOnClickListener {
            showSignOutDialog()
        }
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
        // Sign out from Firebase
        firebaseManager.signOut()

        // Clear SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }

        // Navigate to Login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}