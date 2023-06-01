package com.example.incndex.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.incndex.R
import com.example.incndex.data.PaymentResponse
import com.example.incndex.databinding.ItemPaymentBinding

class PaymentAdapter(var mcontext: Context, var listner: onClickListner) : RecyclerView.Adapter<PaymentAdapter.ViewHolder>() {
    private lateinit var binding: ItemPaymentBinding
    var paymentList: List<PaymentResponse> = emptyList()
    private var selectedItemPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvName.setText(paymentList.get(position).name)
            holder.ibPaymentMode.setOnClickListener {
                listner.onItemClick(position)
                selectedItemPosition = position
                notifyDataSetChanged()
            }

            if(selectedItemPosition == position) {
                holder.ibPaymentMode.setImageResource(paymentList.get(position).profile)
               holder.cardLayout.setBackgroundColor(Color.parseColor("#ffffff"))
                holder.cardLayout.setBackgroundResource(R.drawable.border_box)
            }
            else {
                holder.ibPaymentMode.setImageResource(paymentList.get(position).profile)
                holder.cardLayout.setBackgroundColor(Color.parseColor("#E1E4E1E1"))
                holder.cardLayout.setBackgroundResource(0)
            }

    }

    override fun getItemCount(): Int = paymentList.size


    inner class ViewHolder : RecyclerView.ViewHolder(binding.root) {
        val cardLayout: LinearLayout = binding.linearLayout
        val ibPaymentMode: ImageView = binding.ibPaymentMode
        val tvName: TextView = binding.tvName
    }

    interface onClickListner{
        fun onItemClick(position: Int)
    }

}