package com.varsitycollege.vc_eats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import com.varsitycollege.vc_eats.models.MenuItem

class MenuAdapter(
    private val items: List<MenuItem>,
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit,
    private val onToggleAvailability: (Int) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardItem: MaterialCardView = view as MaterialCardView
        val ivMenuItemImage: ShapeableImageView = view.findViewById(R.id.ivMenuItemImage)
        val tvSoldOutBadge: TextView = view.findViewById(R.id.tvSoldOutBadge)
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val chipCategory: Chip = view.findViewById(R.id.chipCategory)
        val viewStatusIndicator: View = view.findViewById(R.id.viewStatusIndicator)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnViewItem: ImageButton = view.findViewById(R.id.btnViewItem)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditItem)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        // Set basic info
        holder.tvItemName.text = item.name
        holder.tvDescription.text = item.description
        holder.tvPrice.text = "R${String.format("%.2f", item.price)}"

        // Set category chip
        holder.chipCategory.text = item.category

        // Set status
        if (item.isAvailable) {
            holder.tvStatus.text = "Available"
            holder.tvStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
            holder.viewStatusIndicator.setBackgroundColor(context.getColor(android.R.color.holo_green_dark))
            holder.tvSoldOutBadge.visibility = View.GONE
            holder.cardItem.alpha = 1.0f
        } else {
            holder.tvStatus.text = "Sold Out"
            holder.tvStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
            holder.viewStatusIndicator.setBackgroundColor(context.getColor(android.R.color.holo_red_dark))
            holder.tvSoldOutBadge.visibility = View.VISIBLE
            holder.cardItem.alpha = 0.7f
        }

        // Load image from Firebase URL using Glide
        if (item.imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(item.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivMenuItemImage)
        } else {
            holder.ivMenuItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Click listeners
        holder.btnViewItem.setOnClickListener {
            // Toggle availability when view button is clicked
            onToggleAvailability(position)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(position)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount() = items.size
}