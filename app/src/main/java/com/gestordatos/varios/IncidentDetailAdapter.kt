package com.gestordatos.varios


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gestordatos.BBDD.IncidentEntity
import com.gestordatos.R

class IncidentDetailAdapter(private val onItemClick: (IncidentEntity) -> Unit) : ListAdapter<IncidentEntity, IncidentDetailAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incident_detail, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View, private val onItemClick: (IncidentEntity) -> Unit) : RecyclerView.ViewHolder(itemView) {
        // Bind your views here
        private val textoTelefonos: TextView = itemView.findViewById(R.id.tvTelefono)
        private val textoDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        private val textoNombre: TextView = itemView.findViewById(R.id.tvNombreCliente)

        fun bind(item: IncidentEntity) {
            textoNombre.text = item.nombreCliente
            textoDireccion.text = item.domicilio
            textoTelefonos.text = item.telefonos

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<IncidentEntity>() {
        override fun areItemsTheSame(oldItem: IncidentEntity, newItem: IncidentEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: IncidentEntity, newItem: IncidentEntity): Boolean {
            return oldItem == newItem
        }
    }
}