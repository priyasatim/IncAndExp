package com.example.incndex.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.incndex.R
import com.example.incndex.data.Category
import com.example.incndex.data.Expenses
import com.example.incndex.data.Income
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityAddIncomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpensesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddIncomeBinding
    lateinit var userDao : UserDao
    var isComingFromList : Boolean = false
    var arrayList = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivAdd.setBackgroundResource(R.drawable.ic_minus);

        userDao = UserDatabase.getDatabase(applicationContext).userDao()
        if (intent.getBooleanExtra("isList", false)) {
            isComingFromList = intent.getBooleanExtra("isList", false)
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (userDao.readCategory().isNotEmpty()) {
                for (i in userDao.readCategory()) {
                    arrayList.add(i.name)
                }
            }
        }

        binding.tvCategory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()

                val filteredItems: List<String> = arrayList.filter { it.contains(inputText, true) }

                var adapter = ArrayAdapter(
                    this@AddExpensesActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    filteredItems
                )

                binding.tvCategory.threshold = 1;//will start working from first character
                binding.tvCategory.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView

            }
        })



        binding.tvSubmit.setOnClickListener {
            if (validation()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    val currentDate = sdf.format(Date())

                    val user = Expenses(
                        name = binding.etNote.text.trim().toString(),
                        category = binding.tvCategory.text.trim().toString(),
                        price = binding.etAmount.text.trim().toString().toDouble(),
                        date = currentDate
                    )
                    arrayList.add(binding.tvCategory.text.trim().toString())
                    userDao.addCategory(Category(name = binding.tvCategory.text.trim().toString()))
                    userDao.addExpenses(user)

                    this@AddExpensesActivity.finish()
                    var intent =
                        Intent(this@AddExpensesActivity, ListOfExpensesActivity::class.java)
                    startActivity(intent)


                }
            }
        }

    }

        public fun validation() : Boolean{
            if(binding.tvCategory.text.trim().isEmpty()){
                Toast.makeText(this,"Please Enter Category",Toast.LENGTH_LONG).show()
                return false
            }else if(binding.etNote.text.trim().toString().isEmpty()){
                Toast.makeText(this,"Please Enter Note",Toast.LENGTH_LONG).show()
                return false
            }else if(binding.etAmount.text.trim().toString().isEmpty() || binding.etAmount.text.trim().toString() == "0.0"){
                Toast.makeText(this,"Please Enter Amount",Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }

}