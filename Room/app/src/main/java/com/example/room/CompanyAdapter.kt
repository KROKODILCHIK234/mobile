package com.example.room

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CompanyAdapter : ListAdapter<Company, CompanyAdapter.CompanyViewHolder>(CompanyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return CompanyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val company = getItem(position)
        holder.bind(company)
    }

    class CompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(company: Company) {
            text1.text = company.name
            text2.text = company.capitalization.toString()
        }
    }

    class CompanyDiffCallback : DiffUtil.ItemCallback<Company>() {
        override fun areItemsTheSame(oldItem: Company, newItem: Company): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Company, newItem: Company): Boolean = oldItem == newItem
    }
}
