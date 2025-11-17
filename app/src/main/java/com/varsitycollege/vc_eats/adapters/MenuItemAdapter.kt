package com.varsitycollege.vc_eats.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.vc_eats.R
import com.varsitycollege.vc_eats.models.MenuItem

class MenuItemAdapter(
    private val items: List<MenuItem>,
    private val onViewClick: (MenuItem) -> Unit,
    private val onEditClick: (MenuItem) -> Unit,
    private val onDeleteClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuItemAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val btnView: ImageButton = view.findViewById(R.id.btnViewItem)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditItem)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvPrice.text = "R%.2f".format(item.price)
        holder.btnView.setOnClickListener { onViewClick(item) }
        holder.btnEdit.setOnClickListener { onEditClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
