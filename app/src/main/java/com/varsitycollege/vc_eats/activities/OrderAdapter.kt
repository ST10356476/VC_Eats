package com.varsitycollege.vc_eats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.varsitycollege.vc_eats.models.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private val orders: List<Order>,
    private val onStatusChange: (String, String) -> Unit,
    private val onViewDetails: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardOrder: MaterialCardView = view as MaterialCardView
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val viewOrderStatusIndicator: View = view.findViewById(R.id.viewOrderStatusIndicator)
        val tvOrderStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvOrderTime: TextView = view.findViewById(R.id.tvOrderTime)
        val tvEstimatedTime: TextView = view.findViewById(R.id.tvEstimatedTime)
        val layoutOrderItems: LinearLayout = view.findViewById(R.id.layoutOrderItems)
        val tvOrderTotal: TextView = view.findViewById(R.id.tvOrderTotal)
        val btnOrderAction: MaterialButton = view.findViewById(R.id.btnOrderAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        // Order ID
        holder.tvOrderId.text = "Order #${order.id.take(8).uppercase()}"

        // Order time
        val timeFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        holder.tvOrderTime.text = "Ordered: ${timeFormat.format(Date(order.orderTime))}"

        // Estimated time
        if (order.estimatedReadyTime > 0) {
            holder.tvEstimatedTime.text = "Ready: ${timeFormat.format(Date(order.estimatedReadyTime))}"
            holder.tvEstimatedTime.visibility = View.VISIBLE
        } else {
            holder.tvEstimatedTime.visibility = View.GONE
        }

        // Status indicator and text
        when (order.status) {
            "PENDING" -> {
                holder.tvOrderStatus.text = "PENDING"
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark))
                holder.viewOrderStatusIndicator.setBackgroundResource(R.drawable.circle_orange)
                holder.btnOrderAction.text = "Start Preparing"
                holder.btnOrderAction.visibility = View.VISIBLE
                holder.cardOrder.alpha = 1.0f
            }
            "PREPARING" -> {
                holder.tvOrderStatus.text = "PREPARING"
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_blue_dark))
                holder.viewOrderStatusIndicator.setBackgroundResource(R.drawable.circle_blue)
                holder.btnOrderAction.text = "Mark as Ready"
                holder.btnOrderAction.visibility = View.VISIBLE
                holder.cardOrder.alpha = 1.0f
            }
            "READY" -> {
                holder.tvOrderStatus.text = "READY"
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
                holder.viewOrderStatusIndicator.setBackgroundResource(R.drawable.circle_green)
                holder.btnOrderAction.text = "Complete"
                holder.btnOrderAction.visibility = View.VISIBLE
                holder.cardOrder.alpha = 1.0f
            }
            "COMPLETED" -> {
                holder.tvOrderStatus.text = "COMPLETED"
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.darker_gray))
                holder.viewOrderStatusIndicator.setBackgroundResource(R.drawable.circle_gray)
                holder.btnOrderAction.visibility = View.GONE
                holder.cardOrder.alpha = 0.6f
            }
            "CANCELLED" -> {
                holder.tvOrderStatus.text = "CANCELLED"
                holder.tvOrderStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
                holder.viewOrderStatusIndicator.setBackgroundResource(R.drawable.circle_gray)
                holder.btnOrderAction.visibility = View.GONE
                holder.cardOrder.alpha = 0.6f
            }
        }

        // Populate order items dynamically
        holder.layoutOrderItems.removeAllViews()
        for (item in order.items) {
            val itemView = LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_2,
                holder.layoutOrderItems,
                false
            )

            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<TextView>(android.R.id.text2)

            text1.text = "${item.quantity}x ${item.name}"
            text1.textSize = 14f
            text1.setTextColor(context.getColor(android.R.color.black))

            if (item.specialInstructions.isNotEmpty()) {
                text2.text = "Note: ${item.specialInstructions}"
                text2.textSize = 12f
                text2.setTextColor(context.getColor(android.R.color.holo_orange_dark))
                text2.visibility = View.VISIBLE
            } else {
                text2.visibility = View.GONE
            }

            holder.layoutOrderItems.addView(itemView)
        }

        // Total
        holder.tvOrderTotal.text = "Total: R${String.format("%.2f", order.totalAmount)}"

        // Action button click
        holder.btnOrderAction.setOnClickListener {
            val nextStatus = when (order.status) {
                "PENDING" -> "PREPARING"
                "PREPARING" -> "READY"
                "READY" -> "COMPLETED"
                else -> order.status
            }
            onStatusChange(order.id, nextStatus)
        }

        // Card click for details
        holder.cardOrder.setOnClickListener {
            onViewDetails(order)
        }
    }

    override fun getItemCount() = orders.size
}