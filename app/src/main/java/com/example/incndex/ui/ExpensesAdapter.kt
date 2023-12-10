package com.example.incndex.ui

import android.content.Context
import android.graphics.Color
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.incndex.R
import com.example.incndex.data.Amount
import com.example.incndex.data.UserDao
import com.example.incndex.databinding.ItemRecordBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class ExpensesAdapter(var context : Context,var listner : onClickListner, var userDao: UserDao)  : RecyclerView.Adapter<ExpensesAdapter.ViewHolder>(){
    private lateinit var binding: ItemRecordBinding

    private var itemList: ArrayList<Amount> = ArrayList()
    var selectedList = ArrayList<Amount>()
    var filteredItemList: ArrayList<Amount> = ArrayList()

    inner class ViewHolder : RecyclerView.ViewHolder(binding.root){
        fun setData(item : Amount){
            binding.apply {
                tvTitle.text=item.name.toString()
                tvCategory.text=item.category.toString()
                tvDate.text= convertLongToTime(item.date)
                tvAmount.text="₹ " +item.price.toString()
                tvAmount.setTextColor(ContextCompat.getColor(context, R.color.red));

                when(item.payment_mode){
                    "Card" -> {
                        ivPayment.setImageResource(R.drawable.card)
                    }
                    "Online" -> {
                        ivPayment.setImageResource(R.drawable.netbanking)
                    }
                    "Cash" -> {
                        ivPayment.setImageResource(R.drawable.money)
                    }
                    "UPI" -> {
                        ivPayment.setImageResource(R.drawable.upi)
                    }
                }


                chRecord.setOnCheckedChangeListener { _, isChecked ->
                    listner.onSelect(item,isChecked)
                }
                constraintRecord.setOnClickListener {
                    listner?.onBalance(item)

                }
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding= ItemRecordBinding.inflate(LayoutInflater.from(parent.context),parent,false)
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
    fun updateItemList(newItemList: ArrayList<Amount>, query: String, isCategoryfilter : Boolean) {
        val filteredList = if (query.isNotEmpty()) {
            if (isCategoryfilter)
                newItemList.filter { it.category.contains(query, ignoreCase = true) }
            else
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

        Log.d("filteredItemList",""+filteredItemList.size)
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
        fun onSelect(amount : Amount, ischeck : Boolean)
        fun onBalance(income : Amount)

    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd.MM.yyyy")
        return format.format(date)
    }
}