package com.example.incndex.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.incndex.R
import com.example.incndex.data.Amount
import com.example.incndex.data.RestoreData
import com.example.incndex.databinding.ItemRecordBinding
import com.example.incndex.databinding.ItemRestoreBinding
import java.text.SimpleDateFormat
import java.util.Date

class RestoreAdapter(var context: Context,var adapterList : ArrayList<Amount>,var listner : onClickListner) : RecyclerView.Adapter<RestoreAdapter.ViewHolder>() {
    private lateinit var binding: ItemRestoreBinding
    var totalAmount : Double = 0.00

    inner class ViewHolder : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemRestoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder()
    }

    override fun getItemCount(): Int {
        return adapterList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        binding.tvTitle.text = adapterList.get(position).name
        binding.tvCategory.text = adapterList.get(position).category
        binding.tvAmount.text = "₹ ${adapterList.get(position).price.toString()}"
        binding.tvDate.text = convertLongToTime(adapterList.get(position).date)
        if(adapterList.get(position).isIncome){
            binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else
            binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.red));

        when(adapterList.get(position).payment_mode){
            "Card" -> {
                binding.ivPayment.setImageResource(R.drawable.card)
            }
            "Online" -> {
                 binding.ivPayment.setImageResource(R.drawable.netbanking)
            }
            "Cash" -> {
                binding.ivPayment.setImageResource(R.drawable.money)
            }
            "UPI" -> {
                binding.ivPayment.setImageResource(R.drawable.upi)
            }
        }

        if(adapterList.get(position).isIncome){
            totalAmount += adapterList.get(position).price
        } else
            totalAmount -= adapterList.get(position).price

        listner.setAmount(totalAmount)
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd.MM.yyyy")
        return format.format(date)
    }

    interface onClickListner{
        fun setAmount(amount : Double)
    }
}