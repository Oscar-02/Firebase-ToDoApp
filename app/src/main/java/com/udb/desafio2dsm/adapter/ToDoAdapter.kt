package com.udb.desafio2dsm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.udb.desafio2dsm.R
import com.udb.desafio2dsm.model.ToDo

class ToDoAdapter(
    private var items: List<ToDo>,
    private val onEditClick: (ToDo) -> Unit,
    private val onDeleteClick: (ToDo) -> Unit,
    private val onToggleDone: (ToDo) -> Unit
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.tvCreatedAt)
        val tvDoneStatus: TextView = itemView.findViewById(R.id.tvDoneStatus)
        val tvCreatedInfo: TextView = itemView.findViewById(R.id.tvCreatedInfo)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return ToDoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description
        holder.tvCreatedAt.text = item.createdAt.substring(0, 10) // Solo fecha
        holder.tvDoneStatus.text = if (item.done) "✅ Finalizada" else "❌ Pendiente"
        holder.tvCreatedInfo.text = "Creado por: ${item.createdBy} el ${item.createdAt.substring(0, 10)}"

        holder.itemView.setOnLongClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.itemView)
            popup.menuInflater.inflate(R.menu.todo_item_menu, popup.menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_toggle_done -> {
                        onToggleDone(item)
                        true
                    }
                    R.id.menu_edit -> {
                        onEditClick(item)
                        true
                    }
                    R.id.menu_delete -> {
                        onDeleteClick(item)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
            true
        }
    }



    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ToDo>) {
        items = newItems
        notifyDataSetChanged()
    }
}