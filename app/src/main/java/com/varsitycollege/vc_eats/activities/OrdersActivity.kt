package com.varsitycollege.vc_eats

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.vc_eats.api.RetrofitClient
import com.varsitycollege.vc_eats.databinding.ActivityOrdersBinding
import com.varsitycollege.vc_eats.models.Order
import com.varsitycollege.vc_eats.utils.TokenManager
import kotlinx.coroutines.launch
import android.widget.Toast

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private var orders: List<Order> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadOrders()
    }

    private fun loadOrders() {
        val token = TokenManager.getToken()
        if (token == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                orders = RetrofitClient.apiService.getUserOrders("Bearer $token").body() ?: emptyList()
                displayOrders(orders)
            } catch (e: Exception) {
                Toast.makeText(this@OrdersActivity, "Failed to load orders: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayOrders(orders: List<Order>) {
        // Implement your RecyclerView display logic
    }
}
