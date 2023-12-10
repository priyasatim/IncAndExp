package com.example.incndex.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.incndex.R
import com.example.incndex.data.Amount
import com.example.incndex.data.Category
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.FragmentListBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListBottomSheetFragment : BottomSheetDialogFragment(), CategoryAdapter.clickListner {
    private lateinit var adapter: CategoryAdapter
    private lateinit var binding: FragmentListBottomSheetBinding
    lateinit var userDao : UserDao
    private var itemList: ArrayList<Category> = ArrayList()
    var dataPassListener: DataPassListener? = null

    companion object {
        fun newInstance(item: String): ListBottomSheetFragment {
            val args = Bundle()
            args.putString("item", item)
            val fragment = ListBottomSheetFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_list_bottom_sheet, container, false
        )

        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)

        userDao = context?.let { UserDatabase.getDatabase(it).userDao() }!!

        val item = arguments?.getString("item")

        if(!item.isNullOrEmpty()){
        binding.tvClear.visibility = View.VISIBLE
        }

        CoroutineScope(Dispatchers.IO).launch {
            val data = userDao.readCategory() as ArrayList<Category>

            withContext(Dispatchers.Main) {
                itemList.clear()
                itemList.addAll(data)

                binding.recycleview.layoutManager = LinearLayoutManager(activity)
                adapter = CategoryAdapter(requireContext(),itemList, item ?: "", this@ListBottomSheetFragment)
                binding.recycleview.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }

        binding.tvClear.setOnClickListener {
            dataPassListener?.onDataPassed("")
            dialog?.dismiss()
        }

        return binding.root
    }

    override fun onClick(name: String) {
        dataPassListener?.onDataPassed(name)
        dialog?.dismiss()
    }

    interface DataPassListener{
        fun onDataPassed(name: String)
    }
}