package com.example.incndex.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.incndex.R
import com.example.incndex.data.Amount
import com.example.incndex.data.RestoreData
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityListOfIncomeBinding
import com.example.incndex.databinding.ActivityRestoreHistoryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RestoreHistoryActivity : AppCompatActivity(), RestoreAdapter.onClickListner {
    private lateinit var adapter: RestoreAdapter
    private lateinit var binding: ActivityRestoreHistoryBinding
    var list: ArrayList<Amount> = ArrayList()
    lateinit var userDao: UserDao
    var defaultName : String? = null
    var defaultCategory : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestoreHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDatabase.getDatabase(applicationContext).userDao()

        defaultName = intent.getStringExtra("name")
        defaultCategory = intent.getStringExtra("category")

        CoroutineScope(Dispatchers.IO).launch {
            if(!defaultName.isNullOrEmpty() && !defaultCategory.isNullOrEmpty()) {
                for (i in userDao.readAmount(null, null)) {
                    if (i.name == defaultName && i.category == defaultCategory) {
                        list.add(i)
                    }
                }

                if (list.isNotEmpty()) {
                    binding.rvBalance.layoutManager =
                        LinearLayoutManager(this@RestoreHistoryActivity)
                    adapter = RestoreAdapter(
                        this@RestoreHistoryActivity,
                        list,
                        this@RestoreHistoryActivity
                    )
                    binding.rvBalance.adapter = adapter
                }
            }
        }


    }

    override fun setAmount(amount: Double) {
        binding.tvTitleBalanceAmount.text = "₹$amount"
    }
}