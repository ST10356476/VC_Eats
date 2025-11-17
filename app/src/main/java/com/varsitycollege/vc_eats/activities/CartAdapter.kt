package com.varsitycollege.vc_eats.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.vc_eats.databinding.ItemCartBinding
import com.varsitycollege.vc_eats.viewmodels.CartItem
import com.varsitycollege.vc_eats.utils.CartManager

class CartAdapter(
    private var items: List<CartItem>,
    private val onCartUpdated: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = items[position]

        with(holder.binding) {
            tvTitle.text = cartItem.menuItem.name
            tvPriceEach.text = "R%.2f each".format(cartItem.menuItem.price)
            tvQty.text = cartItem.quantity.toString()

            // TODO: Load image with Glide when you have images
            // Glide.with(holder.itemView.context)
            //     .load(cartItem.menuItem.imageUrl)
            //     .into(img)

            btnPlus.setOnClickListener {
                CartManager.updateQuantity(cartItem.menuItem, cartItem.quantity + 1)
                onCartUpdated()
            }

            btnMinus.setOnClickListener {
                if (cartItem.quantity > 1) {
                    CartManager.updateQuantity(cartItem.menuItem, cartItem.quantity - 1)
                } else {
                    CartManager.removeItem(cartItem.menuItem)
                }
                onCartUpdated()
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}