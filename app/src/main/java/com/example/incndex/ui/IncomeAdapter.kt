package com.example.incndex.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.incndex.data.Income
import com.example.incndex.databinding.ItemRecordBinding

class IncomeAdapter : RecyclerView.Adapter<IncomeAdapter.ViewHolder>() {
    private lateinit var binding: ItemRecordBinding

    private var itemList: List<Income> = emptyList()
    private var filteredItemList: List<Income> = emptyList()

    inner class ViewHolder : RecyclerView.ViewHolder(binding.root) {
        fun setData(item: Income) {
            binding.apply {
                tvTitle.text = item.name.toString()
                tvCategory.text = item.category.toString()
                tvDate.text = item.date.toString()
                tvAmount.text = "₹ " + item.price.toString()
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredItemList[position]
        holder.setData(item)
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return filteredItemList.size
        Log.d("filtehhhhr", "" + filteredItemList.size)
    }

    // Update item list and perform search using DiffUtil
    fun updateItemList(newItemList: List<Income>, query: String) {
        val diffCallback = ItemDiffCallback(itemList, newItemList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        itemList = newItemList
        filteredItemList = if (query.isNotEmpty()) {
            itemList.filter { it.name.contains(query, ignoreCase = true) }
        } else {
            itemList
        }

        diffResult.dispatchUpdatesTo(this)
    }

    // DiffUtil callback class
    private class ItemDiffCallback(
        private val oldList: List<Income>,
        private val newList: List<Income>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}