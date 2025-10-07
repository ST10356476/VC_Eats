package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.varsitycollege.vc_eats.databinding.ActivityAlertsBinding

class AlertsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertsBinding
    private var allAlerts = mutableListOf<Alert>()
    private var currentFilter = "ALL" // "ALL" or "UNREAD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        loadAlerts()
    }

    private fun setupClickListeners() {
        // Back Button
        binding.btnAlertsBack.setOnClickListener {
            navigateToCustomerMenu()
        }

        // Mark All Read Button
        binding.btnMarkAllRead.setOnClickListener {
            markAllAsRead()
        }

        // Filter Buttons
        binding.btnAll.setOnClickListener {
            currentFilter = "ALL"
            displayAlerts()
        }

        binding.btnUnread.setOnClickListener {
            currentFilter = "UNREAD"
            displayAlerts()
        }
    }
    private fun navigateToCustomerMenu() {
        val intent = Intent(this, CustomerMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
    private fun loadAlerts() {
        // Sample data - replace with Firebase data later
        allAlerts.clear()
        allAlerts.addAll(
            listOf(
                Alert(
                    id = "1",
                    title = "Order Ready for Pickup",
                    message = "Your Full English Breakfast is ready at the counter.",
                    timestamp = "5 min ago",
                    isRead = false,
                    type = "ORDER",
                    icon = R.drawable.bg_badge_green,
                    hasAction = true,
                    actionText = "View Order"
                ),
                Alert(
                    id = "2",
                    title = "Order Confirmed",
                    message = "Your order #1234 has been confirmed and is being prepared by our kitchen staff.",
                    timestamp = "15 min ago",
                    isRead = false,
                    type = "ORDER",
                    icon = R.drawable.bg_badge_green,
                    hasAction = false,
                    actionText = ""
                ),
                Alert(
                    id = "3",
                    title = "Special Offer",
                    message = "Get 20% off on all breakfast items today! Valid until 11 AM.",
                    timestamp = "1 hour ago",
                    isRead = true,
                    type = "PROMOTION",
                    icon = R.drawable.bg_badge_green,
                    hasAction = true,
                    actionText = "View Menu"
                )
            )
        )

        displayAlerts()
        updateBadges()
    }

    private fun displayAlerts() {
        binding.containerAlerts.removeAllViews()

        val filteredAlerts = when (currentFilter) {
            "UNREAD" -> allAlerts.filter { !it.isRead }
            else -> allAlerts
        }

        if (filteredAlerts.isEmpty()) {
            showEmptyState()
            return
        }

        filteredAlerts.forEach { alert ->
            val alertView = createAlertView(alert)
            binding.containerAlerts.addView(alertView)
        }
    }

    private fun createAlertView(alert: Alert): View {
        val alertCard = layoutInflater.inflate(
            R.layout.item_alert,
            binding.containerAlerts,
            false
        ) as MaterialCardView

        // Find views in the alert card
        val icon = alertCard.findViewById<ImageView>(R.id.icon)
        val tvTitle = alertCard.findViewById<TextView>(R.id.title)
        val tvMessage = alertCard.findViewById<TextView>(R.id.message)
        val tvTimeAgo = alertCard.findViewById<TextView>(R.id.timeAgo)
        val btnDelete = alertCard.findViewById<ImageButton>(R.id.btnDelete)
        val dotUnread = alertCard.findViewById<View>(R.id.dotUnread)
        val btnCta = alertCard.findViewById<MaterialButton>(R.id.btnCta)

        // Set data
        icon.setImageResource(alert.icon)
        tvTitle.text = alert.title
        tvMessage.text = alert.message
        tvTimeAgo.text = alert.timestamp

        // Show/hide unread indicator
        dotUnread.visibility = if (alert.isRead) View.GONE else View.VISIBLE

        // Show/hide action button
        if (alert.hasAction) {
            btnCta.visibility = View.VISIBLE
            btnCta.text = alert.actionText
            btnCta.setOnClickListener {
                handleActionClick(alert)
            }
        } else {
            btnCta.visibility = View.GONE
        }

        // Set card background and stroke based on read status
        if (!alert.isRead) {
            alertCard.setCardBackgroundColor(resources.getColor(R.color.alert_unread_bg, null))
            alertCard.strokeColor = resources.getColor(R.color.alert_unread_stroke, null)
        } else {
            alertCard.setCardBackgroundColor(resources.getColor(android.R.color.white, null))
            alertCard.strokeColor = resources.getColor(R.color.alert_read_stroke, null)
        }

        // Delete button click listener
        btnDelete.setOnClickListener {
            deleteAlert(alert)
        }

        // Card click listener - mark as read
        alertCard.setOnClickListener {
            if (!alert.isRead) {
                markAsRead(alert)
            }
        }

        return alertCard
    }

    private fun handleActionClick(alert: Alert) {
        when (alert.type) {
            "ORDER" -> {
                // Navigate to Orders activity
                // startActivity(Intent(this, OrdersActivity::class.java))
            }
            "PROMOTION" -> {
                // Navigate to Menu activity
                // startActivity(Intent(this, CustomerMenuActivity::class.java))
            }
        }
        markAsRead(alert)
    }

    private fun showEmptyState() {
        val emptyView = layoutInflater.inflate(
            R.layout.empty_state,
            binding.containerAlerts,
            false
        )

        val tvEmpty = emptyView.findViewById<TextView>(R.id.tvEmptyMessage)
        tvEmpty.text = when (currentFilter) {
            "UNREAD" -> "No unread notifications"
            else -> "No notifications yet"
        }

        binding.containerAlerts.addView(emptyView)
    }

    private fun markAsRead(alert: Alert) {
        val index = allAlerts.indexOfFirst { it.id == alert.id }
        if (index != -1) {
            allAlerts[index] = alert.copy(isRead = true)
            displayAlerts()
            updateBadges()
        }
    }

    private fun markAllAsRead() {
        allAlerts = allAlerts.map { it.copy(isRead = true) }.toMutableList()
        displayAlerts()
        updateBadges()
    }

    private fun deleteAlert(alert: Alert) {
        allAlerts.removeAll { it.id == alert.id }
        displayAlerts()
        updateBadges()
    }

    private fun updateBadges() {
        val totalCount = allAlerts.size
        val unreadCount = allAlerts.count { !it.isRead }

        binding.badgeTotal.text = unreadCount.toString()
        binding.badgeTotal.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE

        binding.btnAll.text = "All ($totalCount)"
        binding.btnUnread.text = "Unread ($unreadCount)"
    }

    // Data class for Alert
    data class Alert(
        val id: String,
        val title: String,
        val message: String,
        val timestamp: String,
        val isRead: Boolean,
        val type: String, // "ORDER", "PROMOTION", "SYSTEM", "PAYMENT"
        val icon: Int, // Drawable resource ID
        val hasAction: Boolean,
        val actionText: String
    )
}