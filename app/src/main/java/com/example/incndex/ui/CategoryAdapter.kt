package com.example.incndex.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.incndex.data.Category
import com.example.incndex.databinding.ItemCategoryBinding
import androidx.core.content.ContextCompat
import com.example.incndex.R


class CategoryAdapter(
    var context : Context,
    list: ArrayList<Category>,
    var itemOfCategory: String, var click: clickListner
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private lateinit var binding: ItemCategoryBinding
    private var itemList: ArrayList<Category> = list

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryAdapter.CategoryViewHolder {
        binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = itemList[position]
        holder.categoryName.text = item.name


        if(item.name.equals(itemOfCategory)){
            val vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_checked)
            holder.categoryName.setCompoundDrawablesWithIntrinsicBounds(vectorDrawable, null, null, null);
        }

            holder.categoryName.setOnClickListener {
                click.onClick(item.name)
            }
    }

    override fun getItemCount(): Int = itemList.size

    class CategoryViewHolder(binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        var categoryName: TextView

        init {
            categoryName = binding.bottomSheetText
        }
    }

    interface clickListner{
        fun onClick(name : String)
    }
}