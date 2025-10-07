package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.varsitycollege.vc_eats.databinding.ActivityCustomerMenuBinding
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import com.varsitycollege.vc_eats.models.MenuItem
import kotlinx.coroutines.launch

class CustomerMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerMenuBinding
    private val firebaseManager = FirebaseManager.getInstance()
    private var currentCategory = "Breakfast"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWelcomeMessage()
        setupCategoryButtons()
        setupBottomNavigation()
        loadMenuItems(currentCategory)
    }

    private fun setupWelcomeMessage() {
        val userId = firebaseManager.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                val user = firebaseManager.getUser(userId)
                if (user != null) {
                    val firstName = user.name.split(" ").firstOrNull() ?: "there"
                    binding.tvWelcomeMessage.text = "Welcome back, $firstName!"
                }
            }
        }
    }

    private fun setupCategoryButtons() {
        // Breakfast Category
        binding.cardBreakfast.setOnClickListener {
            selectCategory("Breakfast", binding.cardBreakfast)
        }

        // Lunch Category
        binding.cardLunch.setOnClickListener {
            selectCategory("Lunch", binding.cardLunch)
        }

        // Beverages Category
        binding.cardBeverages.setOnClickListener {
            selectCategory("Beverages", binding.cardBeverages)
        }

        // Snacks Category
        binding.cardSnacks.setOnClickListener {
            selectCategory("Snacks", binding.cardSnacks)
        }

        // Cart Button
        binding.btnCart.setOnClickListener {
            // Navigate to Cart Activity (implement when ready)
            // startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        // Menu Tab (current activity - do nothing or refresh)
        binding.tabMenu.setOnClickListener {
            // Already on menu, optionally refresh
        }

        // Orders Tab
        binding.tabOrders.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        // Alerts Tab
        binding.tabAlerts.setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }

        // Settings Tab
        binding.tabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun selectCategory(category: String, selectedCard: com.google.android.material.card.MaterialCardView) {
        currentCategory = category

        // Reset all category cards to default state
        resetCategoryCards()

        // Highlight selected category
        selectedCard.setCardBackgroundColor(getColor(R.color.category_selected_bg))
        selectedCard.strokeColor = getColor(R.color.category_selected_stroke)
        selectedCard.strokeWidth = 4

        // Update category title
        binding.tvCategoryTitle.text = "$category Menu"

        // Load menu items for selected category
        loadMenuItems(category)
    }

    private fun resetCategoryCards() {
        val defaultBg = getColor(android.R.color.white)
        val defaultStroke = getColor(android.R.color.transparent)

        binding.cardBreakfast.setCardBackgroundColor(defaultBg)
        binding.cardBreakfast.strokeColor = defaultStroke
        binding.cardBreakfast.strokeWidth = 0

        binding.cardLunch.setCardBackgroundColor(defaultBg)
        binding.cardLunch.strokeColor = defaultStroke
        binding.cardLunch.strokeWidth = 0

        binding.cardBeverages.setCardBackgroundColor(defaultBg)
        binding.cardBeverages.strokeColor = defaultStroke
        binding.cardBeverages.strokeWidth = 0

        binding.cardSnacks.setCardBackgroundColor(defaultBg)
        binding.cardSnacks.strokeColor = defaultStroke
        binding.cardSnacks.strokeWidth = 0
    }

    private fun loadMenuItems(category: String) {
        lifecycleScope.launch {
            val allItems = firebaseManager.getAllMenuItems()
            val filteredItems = allItems.filter { it.category == category }

            if (filteredItems.isEmpty()) {
                showEmptyState()
            } else {
                showMenuItems(filteredItems)
            }

            // Update item count
            binding.tvItemCount.text = "${filteredItems.size} items"
        }
    }

    private fun showEmptyState() {
        binding.recyclerMenuItems.visibility = View.GONE
        binding.layoutEmptyMenu.visibility = View.VISIBLE
    }

    private fun showMenuItems(items: List<MenuItem>) {
        binding.recyclerMenuItems.visibility = View.VISIBLE
        binding.layoutEmptyMenu.visibility = View.GONE

        // Setup RecyclerView (you'll need to create an adapter)
        binding.recyclerMenuItems.layoutManager = LinearLayoutManager(this)
        // binding.recyclerMenuItems.adapter = MenuItemAdapter(items) // Implement adapter
    }

    override fun onResume() {
        super.onResume()
        // Highlight Menu tab as active
        highlightActiveTab(binding.tabMenu)
    }

    private fun highlightActiveTab(activeTab: android.widget.LinearLayout) {
        // Reset all tabs
        resetTabColors()

        // Highlight active tab
        val textView = activeTab.getChildAt(1) as android.widget.TextView
        textView.setTextColor(getColor(R.color.tab_active))
        textView.textSize = 12f
        textView.setTypeface(null, android.graphics.Typeface.BOLD)
    }

    private fun resetTabColors() {
        val inactiveColor = getColor(R.color.tab_inactive)

        // Menu tab
        (binding.tabMenu.getChildAt(1) as android.widget.TextView).setTextColor(inactiveColor)

        // Orders tab
        (binding.tabOrders.getChildAt(1) as android.widget.TextView).setTextColor(inactiveColor)

        // Alerts tab
        (binding.tabAlerts.getChildAt(1) as android.widget.TextView).setTextColor(inactiveColor)

        // Settings tab
        (binding.tabSettings.getChildAt(1) as android.widget.TextView).setTextColor(inactiveColor)
    }
}