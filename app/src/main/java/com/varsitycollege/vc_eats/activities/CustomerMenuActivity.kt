package com.varsitycollege.vc_eats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.varsitycollege.vc_eats.adapters.CartAdapter
import com.varsitycollege.vc_eats.databinding.ActivityCustomerMenuBinding
import com.varsitycollege.vc_eats.databinding.DialogCartBinding
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import com.varsitycollege.vc_eats.models.MenuItem
import com.varsitycollege.vc_eats.utils.CartManager
import com.varsitycollege.vc_eats.utils.LocaleHelper
import kotlinx.coroutines.launch
import android.widget.Toast

class CustomerMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerMenuBinding
    private val firebaseManager = FirebaseManager.getInstance()
    private var currentCategory = "Breakfast"

    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

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
                    binding.tvWelcomeMessage.text = "${getString(R.string.welcome_back)}, $firstName!"
                }
            }
        } else {
            binding.tvWelcomeMessage.text = getString(R.string.welcome_back)
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

        // Cart Button - NOW FUNCTIONAL
        binding.btnCart.setOnClickListener {
            showCartDialog()
        }
    }

    private fun showCartDialog() {
        val dialogBinding = DialogCartBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        fun updateCartUI() {
            val cartItems = CartManager.getItems()

            if (cartItems.isEmpty()) {
                // Show empty state
                dialogBinding.layoutEmpty.visibility = View.VISIBLE
                dialogBinding.layoutFilled.visibility = View.GONE
                dialogBinding.badgeCount.visibility = View.GONE
            } else {
                // Show filled state
                dialogBinding.layoutEmpty.visibility = View.GONE
                dialogBinding.layoutFilled.visibility = View.VISIBLE
                dialogBinding.badgeCount.visibility = View.VISIBLE

                // Update badge count
                dialogBinding.badgeCount.text = CartManager.getItemCount().toString()

                // Update summary
                dialogBinding.tvSubtotal.text = "R%.2f".format(CartManager.getSubtotal())
                dialogBinding.tvFee.text = "R%.2f".format(CartManager.getServiceFee())
                dialogBinding.tvTotal.text = "R%.2f".format(CartManager.getTotal())

                // Update checkout button text
                dialogBinding.btnCheckout.text = "Proceed to Payment - R%.2f".format(CartManager.getTotal())

                // Update adapter
                (dialogBinding.recyclerCart.adapter as? CartAdapter)?.updateItems(cartItems)
            }
        }

        // Setup RecyclerView
        val adapter = CartAdapter(CartManager.getItems()) {
            updateCartUI()
        }

        dialogBinding.recyclerCart.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerCart.adapter = adapter

        // Close button
        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // Checkout button
        dialogBinding.btnCheckout.setOnClickListener {
            // TODO: Navigate to payment/checkout
            Toast.makeText(this, "Proceeding to payment...", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // Initial UI update
        updateCartUI()

        dialog.show()
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

        // Update category title with translated category name
        val categoryTitle = when (category) {
            "Breakfast" -> getString(R.string.breakfast)
            "Lunch" -> getString(R.string.lunch)
            "Beverages" -> getString(R.string.beverages)
            "Snacks" -> getString(R.string.snacks)
            else -> category
        }
        binding.tvCategoryTitle.text = "$categoryTitle ${getString(R.string.menu)}"

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

            // Update item count with translated text
            val itemsText = if (filteredItems.size == 1) "item" else "items"
            binding.tvItemCount.text = "${filteredItems.size} $itemsText"
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