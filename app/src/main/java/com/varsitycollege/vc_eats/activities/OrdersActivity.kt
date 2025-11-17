package com.varsitycollege.vc_eats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.protobuf.LazyStringArrayList.emptyList
import com.varsitycollege.vc_eats.api.RetrofitClient
import com.varsitycollege.vc_eats.databinding.ActivityOrdersBinding
import com.varsitycollege.vc_eats.databinding.ItemOrderBinding
import com.varsitycollege.vc_eats.models.Order
import com.varsitycollege.vc_eats.utils.TokenManager
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        loadOrders()
    }

    private fun loadOrders() {
        val token = TokenManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUserOrders("Bearer $token")

                if (!response.isSuccessful) {
                    Toast.makeText(
                        this@OrdersActivity,
                        "Error loading orders: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val orders: List<Order> = (response.body() ?: emptyList()) as List<Order>
                displayOrders(orders)

            } catch (e: Exception) {
                Toast.makeText(
                    this@OrdersActivity,
                    "Failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun displayOrders(orders: List<Order>) {
        val activeContainer: LinearLayout = binding.containerActiveOrders
        val pastContainer: LinearLayout = binding.containerPastOrders

        activeContainer.removeAllViews()
        pastContainer.removeAllViews()

        val activeOrders = orders.filter { it.status.lowercase() != "delivered" }
        val pastOrders = orders.filter { it.status.lowercase() == "delivered" }

        binding.tvNoActive.visibility = if (activeOrders.isEmpty()) View.VISIBLE else View.GONE
        binding.tvNoPast.visibility = if (pastOrders.isEmpty()) View.VISIBLE else View.GONE

        activeOrders.forEach { addOrderCard(it, activeContainer) }
        pastOrders.forEach { addOrderCard(it, pastContainer) }
    }

    private fun addOrderCard(order: Order, container: LinearLayout) {
        val orderBinding = ItemOrderBinding.inflate(LayoutInflater.from(this))
        orderBinding.tvOrderId.text = "Order #${order.id}"
        orderBinding.tvOrderStatus.text = order.status
        orderBinding.tvOrderTotal.text = "R ${order.totalAmount}"
        orderBinding.tvOrderTime.text = (order.orderTime ?: "") as CharSequence?
        orderBinding.tvEstimatedTime.text = (order.estimatedReadyTime ?: "") as CharSequence?

        // Remove previous items
        orderBinding.layoutOrderItems.removeAllViews()

        // Dynamically add order items as TextViews
        order.items.forEach { item ->
            val itemTextView = TextView(this).apply {
                text = "${item.name} x${item.quantity} - R${item.price}"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.black))
            }
            orderBinding.layoutOrderItems.addView(itemTextView)
        }

        // Add the order card to the container
        container.addView(orderBinding.root)
    }
}
