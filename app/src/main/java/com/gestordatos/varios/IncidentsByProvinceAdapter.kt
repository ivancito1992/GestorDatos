package com.gestordatos.varios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gestordatos.BBDD.ProvinceIncidentCount
import com.gestordatos.R

class IncidentsByProvinceAdapter(private val onItemClick: (String) -> Unit) :
    ListAdapter<ProvinceIncidentCount, IncidentsByProvinceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_province_incident, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View, private val onItemClick: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val provinciaTextView: TextView = itemView.findViewById(R.id.provinciaTextView)
        private val countTextView: TextView = itemView.findViewById(R.id.countTextView)

        fun bind(item: ProvinceIncidentCount) {
            provinciaTextView.text = item.provincia
            countTextView.text = item.count.toString()
            itemView.setOnClickListener { onItemClick(item.provincia) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ProvinceIncidentCount>() {
        override fun areItemsTheSame(oldItem: ProvinceIncidentCount, newItem: ProvinceIncidentCount): Boolean {
            return oldItem.provincia == newItem.provincia
        }

        override fun areContentsTheSame(oldItem: ProvinceIncidentCount, newItem: ProvinceIncidentCount): Boolean {
            return oldItem == newItem
        }
    }
}