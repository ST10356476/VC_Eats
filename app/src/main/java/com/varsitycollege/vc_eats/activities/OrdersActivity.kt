package com.varsitycollege.vc_eats
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.vc_eats.databinding.ActivityOrdersBinding
import com.varsitycollege.vc_eats.firebase.FirebaseManager
import com.varsitycollege.vc_eats.models.Order
import com.varsitycollege.vc_eats.utils.LocaleHelper
import kotlinx.coroutines.launch

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private val firebaseManager = FirebaseManager.getInstance()

    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        loadOrders()
    }

    private fun setupClickListeners() {
        // Back Button
        binding.btnBack.setOnClickListener {
            navigateToCustomerMenu()
        }
    }

    private fun navigateToCustomerMenu() {
        val intent = Intent(this, CustomerMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }


    private fun loadOrders() {
        val userId = firebaseManager.getCurrentUserId()

        if (userId == null) {
            showEmptyStates()
            return
        }

        lifecycleScope.launch {
            val orders = firebaseManager.getUserOrders(userId)

            if (orders.isEmpty()) {
                showEmptyStates()
            } else {
                displayOrders(orders)
            }
        }
    }

    private fun displayOrders(orders: List<Order>) {
        // Separate active and past orders
        val activeOrders = orders.filter {
            it.status in listOf("PENDING", "PREPARING", "READY")
        }
        val pastOrders = orders.filter {
            it.status in listOf("COMPLETED", "CANCELLED")
        }

        // Display active orders
        if (activeOrders.isEmpty()) {
            binding.tvNoActive.visibility = View.VISIBLE
            binding.containerActiveOrders.removeAllViews()
        } else {
            binding.tvNoActive.visibility = View.GONE
            displayActiveOrders(activeOrders)
        }

        // Display past orders
        if (pastOrders.isEmpty()) {
            binding.tvNoPast.visibility = View.VISIBLE
            binding.containerPastOrders.removeAllViews()
        } else {
            binding.tvNoPast.visibility = View.GONE
            displayPastOrders(pastOrders)
        }
    }

    private fun displayActiveOrders(orders: List<Order>) {
        binding.containerActiveOrders.removeAllViews()

        orders.forEach { order ->
            val orderView = layoutInflater.inflate(
                R.layout.item_order_card,
                binding.containerActiveOrders,
                false
            )

            // Set order data to the view (you'll need to implement this based on your item_order_card layout)
            // Example:
            // orderView.findViewById<TextView>(R.id.tvOrderId).text = "Order #${order.id}"
            // orderView.findViewById<TextView>(R.id.tvOrderStatus).text = order.status
            // etc.

            binding.containerActiveOrders.addView(orderView)
        }
    }

    private fun displayPastOrders(orders: List<Order>) {
        binding.containerPastOrders.removeAllViews()

        orders.forEach { order ->
            val orderView = layoutInflater.inflate(
                R.layout.item_order_card,
                binding.containerPastOrders,
                false
            )

            // Set order data to the view

            binding.containerPastOrders.addView(orderView)
        }
    }

    private fun showEmptyStates() {
        binding.tvNoActive.visibility = View.VISIBLE
        binding.tvNoPast.visibility = View.VISIBLE
        binding.containerActiveOrders.removeAllViews()
        binding.containerPastOrders.removeAllViews()
    }
}