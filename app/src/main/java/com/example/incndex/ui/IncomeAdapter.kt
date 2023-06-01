package com.example.incndex.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.incndex.data.Amount
import com.example.incndex.databinding.ItemRecordBinding
import java.text.SimpleDateFormat
import java.util.Date

class IncomeAdapter(var listner : onClickListner) : RecyclerView.Adapter<IncomeAdapter.ViewHolder>() {
    private lateinit var binding: ItemRecordBinding

    private var itemList: ArrayList<Amount> = ArrayList()

    var selectedList = ArrayList<Amount>()
    var filteredItemList: ArrayList<Amount> = ArrayList()

    inner class ViewHolder : RecyclerView.ViewHolder(binding.root) {
        fun setData(item: Amount) {
            binding.apply {
                tvTitle.text = item.name.toString()
                tvCategory.text = item.category.toString()
                tvDate.text= convertLongToTime(item.date)
                tvAmount.text = "₹ " + item.price.toString()
                tvPayment.text = item.payment_mode.toString()

                chRecord.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked){
                            selectedList.add(item)
                            listner.onDelete(selectedList)
                    }
                    else
                    {
                   if(!selectedList.isNullOrEmpty()){
                       for(i in selectedList.withIndex()){
                           if(item.id == i.value.id) {
                               selectedList.remove(item)
                               listner.onDelete(selectedList)
                               break

                           }
                           }
                       }

                   }
                }
                tvRestore.setOnClickListener {
                    listner.onRestore(item)
                }

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
    }

    // Update item list and perform search using DiffUtil
    fun updateItemList(newItemList: List<Amount>, query: String) {
        val filteredList = if (query.isNotEmpty()) {
            newItemList.filter { it.name.contains(query, ignoreCase = true) }
        } else {
            newItemList
        }

        val diffCallback = ItemDiffCallback(filteredItemList, filteredList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        itemList.clear()
        itemList.addAll(newItemList)
        filteredItemList.clear()
        filteredItemList.addAll(filteredList)

        diffResult.dispatchUpdatesTo(this)
    }

    // DiffUtil callback class
    private class ItemDiffCallback(
        private val oldList: List<Amount>,
        private val newList: List<Amount>
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
    interface onClickListner{
        fun onDelete(income : List<Amount>)
        fun onRestore(income : Amount)
    }
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }
}