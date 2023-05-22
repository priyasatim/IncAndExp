package com.example.incndex.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityDashboardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    lateinit var userDao : UserDao

    var totalIncome : Double = 0.0
    var totalExpenses : Double = 0.0
    var total : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDatabase.getDatabase(applicationContext).userDao()

        CoroutineScope(Dispatchers.IO).launch {
            if(userDao.readIncome().isNotEmpty()) {
                for (i in userDao.readIncome()) {
                    totalIncome += i.price
                }
            }

            if(userDao.readExpenses().isNotEmpty()) {
                for (i in userDao.readExpenses()) {
                    totalExpenses += i.price
                }
                binding.tvExpenses.text = totalExpenses.toString()
            }

            if(totalIncome > totalExpenses){
                total = totalIncome - totalExpenses
                binding.tvIncome.text = total.toString()
            } else
                binding.tvIncome.text = totalIncome.toString()

            if(total > 1000 || totalExpenses > 1000){
                binding.tvK.visibility = View.VISIBLE
                binding.tvExcel.visibility = View.VISIBLE
            } else {
                binding.tvK.visibility = View.GONE
                binding.tvExcel.visibility = View.GONE
            }
        }

        binding.tvIncome.setOnClickListener {
                var intent = Intent(this, AddIncomeActivity::class.java)
                startActivity(intent)
        }

        binding.tvExpenses.setOnClickListener {
                var intent = Intent(this, AddExpensesActivity::class.java)
                startActivity(intent)
        }

        binding.tvK.setOnClickListener {
            if(totalIncome > totalExpenses){
                total = totalIncome - totalExpenses
                binding.tvIncome.text = getFormatedNumber(total)
            } else
                binding.tvIncome.text = getFormatedNumber(totalIncome)

            binding.tvExpenses.text = getFormatedNumber(totalExpenses)
        }

        binding.tvExcel.setOnClickListener {
        }
    }

    fun getFormatedNumber(number: Double): String {
        val df = DecimalFormat("#.#")
        var numberString = ""

        numberString = if (Math.abs(number / 1000000) >= 1) {
            df.format(number / 1000000.0) + "m"
        } else if (Math.abs(number / 1000.0) >= 1) {
            df.format(number / 1000.0) + "k"
        } else {
            number.toString()
        }
        return numberString
    }

    override fun onRestart() {
        super.onRestart()

        CoroutineScope(Dispatchers.IO).launch {
            totalIncome  = 0.0
            totalExpenses  = 0.0

            if(userDao.readIncome().isNotEmpty()) {
                for (i in userDao.readIncome()) {
                    totalIncome += i.price
                }
            }

            if(userDao.readExpenses().isNotEmpty()) {
                for (i in userDao.readExpenses()) {
                    totalExpenses += i.price
                }
            }

            withContext(Dispatchers.Main) {
                binding.tvExpenses.text = totalExpenses.toString()

                if (totalIncome > 1000 || totalExpenses > 1000) {
                    binding.tvK.visibility = View.VISIBLE
                    binding.tvExcel.visibility = View.VISIBLE
                } else {
                    binding.tvK.visibility = View.GONE
                    binding.tvExcel.visibility = View.GONE
                }

                if (totalIncome > totalExpenses) {
                    val total = totalIncome - totalExpenses
                    binding.tvIncome.text = total.toString()
                }
            }
        }
    }
}