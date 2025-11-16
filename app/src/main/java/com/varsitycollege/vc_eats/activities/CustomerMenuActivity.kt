package com.varsitycollege.vc_eats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import com.varsitycollege.vc_eats.adapters.CartAdapter
import com.varsitycollege.vc_eats.databinding.ActivityCustomerMenuBinding
import com.varsitycollege.vc_eats.databinding.DialogCartBinding
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import com.varsitycollege.vc_eats.models.MenuItem
import com.varsitycollege.vc_eats.utils.CartManager
import com.varsitycollege.vc_eats.utils.LocaleHelper
import kotlinx.coroutines.launch

class CustomerMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerMenuBinding
    private val firebaseManager = FirebaseManager.getInstance()

    // These match Firestore category values, so they stay in English internally
    private var currentCategory = CATEGORY_BREAKFAST

    // To detect when user changes language in Settings while this activity is in back stack
    private var currentLanguageCode: String? = null

    companion object {
        private const val CATEGORY_BREAKFAST = "Breakfast"
        private const val CATEGORY_LUNCH = "Lunch"
        private const val CATEGORY_BEVERAGES = "Beverages"
        private const val CATEGORY_SNACKS = "Snacks"
    }

    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Remember which language this activity was created with
        currentLanguageCode = LocaleHelper.getLanguage(this)

        setupWelcomeMessage()
        setupCategoryButtons()
        setupBottomNavigation()
        setupInitialCategoryTitle()
        loadMenuItems(currentCategory)
        highlightActiveTab(binding.tabMenu)
    }

    override fun onResume() {
        super.onResume()

        // If language changed since this screen was created, recreate to reload resources
        val langNow = LocaleHelper.getLanguage(this)
        if (currentLanguageCode != null && currentLanguageCode != langNow) {
            currentLanguageCode = langNow
            recreate()
            return
        }

        highlightActiveTab(binding.tabMenu)
    }

    // region Setup UI

    private fun setupWelcomeMessage() {
        val userId = firebaseManager.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                val user = firebaseManager.getUser(userId)
                if (user != null) {
                    val firstName = user.name.split(" ").firstOrNull()
                        ?: getString(R.string.welcome_name_fallback)
                    // Example: "Welcome back, Dani!"
                    binding.tvWelcomeMessage.text =
                        getString(R.string.welcome_back_with_name, firstName)
                } else {
                    binding.tvWelcomeMessage.text = getString(R.string.welcome_back)
                }
            }
        } else {
            binding.tvWelcomeMessage.text = getString(R.string.welcome_back)
        }
    }

    private fun setupInitialCategoryTitle() {
        // Default category title: e.g. "Breakfast Menu"
        val categoryTitle = getString(R.string.breakfast)
        binding.tvCategoryTitle.text =
            getString(R.string.category_menu_title, categoryTitle)
        // Item count starts from XML as @string/zero_items
    }

    private fun setupCategoryButtons() {
        binding.cardBreakfast.setOnClickListener {
            selectCategory(CATEGORY_BREAKFAST, binding.cardBreakfast)
        }

        binding.cardLunch.setOnClickListener {
            selectCategory(CATEGORY_LUNCH, binding.cardLunch)
        }

        binding.cardBeverages.setOnClickListener {
            selectCategory(CATEGORY_BEVERAGES, binding.cardBeverages)
        }

        binding.cardSnacks.setOnClickListener {
            selectCategory(CATEGORY_SNACKS, binding.cardSnacks)
        }

        binding.btnCart.setOnClickListener {
            showCartDialog()
        }
    }

    private fun setupBottomNavigation() {
        binding.tabMenu.setOnClickListener {
            // Already here, could scroll to top if you want
        }

        binding.tabOrders.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        binding.tabAlerts.setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }

        binding.tabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // endregion

    // region Cart dialog

    private fun showCartDialog() {
        val dialogBinding = DialogCartBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        fun updateCartUI() {
            val cartItems = CartManager.getItems()

            if (cartItems.isEmpty()) {
                dialogBinding.layoutEmpty.visibility = View.VISIBLE
                dialogBinding.layoutFilled.visibility = View.GONE
                dialogBinding.badgeCount.visibility = View.GONE
            } else {
                dialogBinding.layoutEmpty.visibility = View.GONE
                dialogBinding.layoutFilled.visibility = View.VISIBLE
                dialogBinding.badgeCount.visibility = View.VISIBLE

                dialogBinding.badgeCount.text = CartManager.getItemCount().toString()

                dialogBinding.tvSubtotal.text =
                    getString(R.string.cart_currency_format, CartManager.getSubtotal())
                dialogBinding.tvFee.text =
                    getString(R.string.cart_currency_format, CartManager.getServiceFee())
                dialogBinding.tvTotal.text =
                    getString(R.string.cart_currency_format, CartManager.getTotal())

                dialogBinding.btnCheckout.text = getString(
                    R.string.cart_proceed_to_payment_with_total,
                    CartManager.getTotal()
                )

                (dialogBinding.recyclerCart.adapter as? CartAdapter)?.updateItems(cartItems)
            }
        }

        val adapter = CartAdapter(CartManager.getItems()) {
            updateCartUI()
        }

        dialogBinding.recyclerCart.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerCart.adapter = adapter

        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnCheckout.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.cart_proceeding_to_payment_toast),
                Toast.LENGTH_SHORT
            ).show()
            // TODO: navigate to payment screen later
            dialog.dismiss()
        }

        updateCartUI()
        dialog.show()
    }

    // endregion

    // region Categories and menu data

    private fun selectCategory(category: String, selectedCard: MaterialCardView) {
        currentCategory = category

        resetCategoryCards()

        selectedCard.setCardBackgroundColor(getColor(R.color.category_selected_bg))
        selectedCard.strokeColor = getColor(R.color.category_selected_stroke)
        selectedCard.strokeWidth = 4

        val categoryTitle = when (category) {
            CATEGORY_BREAKFAST -> getString(R.string.breakfast)
            CATEGORY_LUNCH -> getString(R.string.lunch)
            CATEGORY_BEVERAGES -> getString(R.string.beverages)
            CATEGORY_SNACKS -> getString(R.string.snacks)
            else -> category
        }

        binding.tvCategoryTitle.text =
            getString(R.string.category_menu_title, categoryTitle)

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

            val itemsText = if (filteredItems.size == 1) {
                getString(R.string.item_singular)
            } else {
                getString(R.string.item_plural)
            }

            binding.tvItemCount.text = getString(
                R.string.item_count_format,
                filteredItems.size,
                itemsText
            )
        }
    }

    private fun showEmptyState() {
        binding.recyclerMenuItems.visibility = View.GONE
        binding.layoutEmptyMenu.visibility = View.VISIBLE
    }

    private fun showMenuItems(items: List<MenuItem>) {
        binding.recyclerMenuItems.visibility = View.VISIBLE
        binding.layoutEmptyMenu.visibility = View.GONE

        binding.recyclerMenuItems.layoutManager = LinearLayoutManager(this)
        // TODO: attach your real adapter here
        // binding.recyclerMenuItems.adapter = MenuItemAdapter(items)
    }

    // endregion

    // region Bottom nav highlighting

    private fun highlightActiveTab(activeTab: LinearLayout) {
        resetTabColors()
        val textView = activeTab.getChildAt(1) as TextView
        textView.setTextColor(getColor(R.color.tab_active))
        textView.textSize = 12f
        textView.setTypeface(null, android.graphics.Typeface.BOLD)
    }

    private fun resetTabColors() {
        val inactiveColor = getColor(R.color.tab_inactive)

        (binding.tabMenu.getChildAt(1) as TextView).setTextColor(inactiveColor)
        (binding.tabOrders.getChildAt(1) as TextView).setTextColor(inactiveColor)
        (binding.tabAlerts.getChildAt(1) as TextView).setTextColor(inactiveColor)
        (binding.tabSettings.getChildAt(1) as TextView).setTextColor(inactiveColor)
    }

    // endregion
}
