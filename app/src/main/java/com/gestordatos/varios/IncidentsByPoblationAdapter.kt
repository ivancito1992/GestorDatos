package com.gestordatos.varios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gestordatos.BBDD.PoblationIncidentCount
import com.gestordatos.BBDD.ProvinceIncidentCount
import com.gestordatos.R

class IncidentsByPoblationAdapter(private val onItemClick: (String) -> Unit) :
ListAdapter<PoblationIncidentCount, IncidentsByPoblationAdapter.ViewHolder>(DiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poblation_incident, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View, private val onItemClick: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val poblationTextView: TextView = itemView.findViewById(R.id.poblationTextView)
        private val countTextView: TextView = itemView.findViewById(R.id.countTextView)

        fun bind(item: PoblationIncidentCount) {
            poblationTextView.text = item.poblacion
            countTextView.text = item.count.toString()
            itemView.setOnClickListener { onItemClick(item.poblacion) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PoblationIncidentCount>() {
        override fun areItemsTheSame(oldItem: PoblationIncidentCount, newItem: PoblationIncidentCount): Boolean {
            return oldItem.poblacion == newItem.poblacion
        }

        override fun areContentsTheSame(oldItem: PoblationIncidentCount, newItem: PoblationIncidentCount): Boolean {
            return oldItem == newItem
        }
    }
}