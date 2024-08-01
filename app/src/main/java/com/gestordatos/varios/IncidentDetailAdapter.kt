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

class IncidentDetailAdapter : ListAdapter<IncidentEntity, IncidentDetailAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incident_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind your views here
        private val textoProyecto: TextView = itemView.findViewById(R.id.tvProyecto)
        private val textoPoblacion: TextView = itemView.findViewById(R.id.tvPoblacion)
        private val textoNombre: TextView = itemView.findViewById(R.id.tvNombreCliente)
        fun bind(item: IncidentEntity) {
            textoProyecto.text = item.proyecto
            textoPoblacion.text = item.poblacion
            textoNombre.text = item.nombreCliente
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