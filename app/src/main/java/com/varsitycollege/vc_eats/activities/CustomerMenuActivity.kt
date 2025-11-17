package com.varsitycollege.vc_eats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsitycollege.vc_eats.adapters.CartAdapter
import com.varsitycollege.vc_eats.adapters.MenuItemAdapter
import com.varsitycollege.vc_eats.api.RetrofitClient
import com.varsitycollege.vc_eats.databinding.ActivityCustomerMenuBinding
import com.varsitycollege.vc_eats.databinding.DialogCartBinding
import com.varsitycollege.vc_eats.models.MenuItem
import com.varsitycollege.vc_eats.utils.CartManager
import com.varsitycollege.vc_eats.utils.TokenManager
import kotlinx.coroutines.launch

class CustomerMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerMenuBinding
    private var menuItems: List<MenuItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        loadMenuItems()
    }

    private fun loadMenuItems() {
        val token = TokenManager.getToken()
        if (token == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMenuItems("Bearer $token")
                if (response.isSuccessful) {
                    val menuResponse = response.body()
                    menuItems = menuResponse?.data?.menuItems ?: emptyList()
                    displayMenuItems(menuItems)
                } else {
                    Toast.makeText(this@CustomerMenuActivity, "Error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomerMenuActivity, "Failed to load menu: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayMenuItems(items: List<MenuItem>) {
        if (items.isEmpty()) {
            binding.recyclerMenuItems.visibility = android.view.View.GONE
            binding.layoutEmptyMenu.visibility = android.view.View.VISIBLE
            return
        }

        binding.recyclerMenuItems.visibility = android.view.View.VISIBLE
        binding.layoutEmptyMenu.visibility = android.view.View.GONE

        val adapter = MenuItemAdapter(
            menuItems,
            onViewClick = { /* handle view */ },
            onEditClick = { /* handle edit */ },
            onDeleteClick = { /* handle delete */ },
            onCartUpdated = { updateCartDialog(DialogCartBinding.inflate(LayoutInflater.from(this))) }
        )

        binding.recyclerMenuItems.adapter = adapter
        binding.recyclerMenuItems.layoutManager = LinearLayoutManager(this)

        // Add click to open cart
        binding.btnCart.setOnClickListener { showCartDialog() }
    }

    private fun showCartDialog() {
        val dialogBinding = DialogCartBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)

        val cartAdapter = CartAdapter(CartManager.getItems()) {
            updateCartDialog(dialogBinding) // update totals
        }

        dialogBinding.recyclerCart.adapter = cartAdapter
        dialogBinding.recyclerCart.layoutManager = LinearLayoutManager(this)

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnCheckout.setOnClickListener {
            Toast.makeText(this, "Proceeding to payment: R%.2f".format(CartManager.getTotal()), Toast.LENGTH_SHORT).show()
        }

        updateCartDialog(dialogBinding)

        dialog.show()
    }

    private fun updateCartDialog(binding: DialogCartBinding) {
        val items = CartManager.getItems()
        binding.layoutEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        binding.layoutFilled.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE

        binding.tvSubtotal.text = "R%.2f".format(CartManager.getSubtotal())
        binding.tvFee.text = "R%.2f".format(CartManager.getServiceFee())
        binding.tvTotal.text = "R%.2f".format(CartManager.getTotal())
    }


    private fun setupBottomNavigation() {
        binding.tabMenu.setOnClickListener {
            // Already in Menu, maybe highlight it
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
}
