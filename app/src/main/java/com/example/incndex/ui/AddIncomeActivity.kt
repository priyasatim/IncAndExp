package com.example.incndex.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.incndex.R
import com.example.incndex.data.Category
import com.example.incndex.data.Income
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityAddIncomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


public class AddIncomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddIncomeBinding
    lateinit var userDao : UserDao
    var isComingFromList : Boolean = false
    var arrayList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivAdd.setBackgroundResource(R.drawable.ic_add);
        userDao = UserDatabase.getDatabase(applicationContext).userDao()

        if(intent.getBooleanExtra("isList",false)){
            isComingFromList = intent.getBooleanExtra("isList",false)
        }


        CoroutineScope(Dispatchers.IO).launch {
            if (userDao.readCategory().isNotEmpty()) {
                for (i in userDao.readCategory()) {
                    arrayList.add(i.name)
                }
            }
        }

        binding.tvSelectDate.setOnClickListener {
            showDatePickerDialog()
        }


        binding.tvCategory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()

                val filteredItems : List<String> = arrayList.filter { it.contains(inputText, true) }
                if(filteredItems.size == 0) binding.tvAddCategory.visibility = View.VISIBLE else binding.tvAddCategory.visibility = View.GONE

                var adapter = ArrayAdapter(this@AddIncomeActivity, android.R.layout.simple_dropdown_item_1line, filteredItems)

                binding.tvCategory.threshold = 1;//will start working from first character
                binding.tvCategory.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView

            }
        })

        binding.tvAddCategory.setOnClickListener {
            binding.tvAddCategory.visibility= View.GONE
            arrayList.add(binding.tvCategory.text.trim().toString())
            CoroutineScope(Dispatchers.IO).launch {
                userDao.addCategory(Category(name = binding.tvCategory.text.trim().toString()))
            }
            Toast.makeText(this,"Successfully Category Added",Toast.LENGTH_LONG).show()
        }


        binding.tvSubmit.setOnClickListener {
            if (validation()) {
                CoroutineScope(Dispatchers.IO).launch {

                    val user = Income(
                        name = binding.etNote.text.trim().toString(),
                        category = binding.tvCategory.text.trim().toString(),
                        price = binding.etAmount.text.trim().toString().toDouble(),
                        date = binding.tvSelectDate.text.toString()
                    )
                    userDao.addIncome(user)

                    if (isComingFromList) {
                        setResult(Activity.RESULT_OK)
                        this@AddIncomeActivity.finish()

                    } else {
                        this@AddIncomeActivity.finish()
                        var intent =
                            Intent(this@AddIncomeActivity, ListOfIncomeActivity::class.java)
                        startActivity(intent)
                    }

                }
            }
        }
        binding.ivClose.setOnClickListener {
            this.finish()
        }


    }

    private fun showDatePickerDialog()  {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Handle the selected date
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val dateString = dateFormat.format(calendar.time)
                binding.tvSelectDate.text = dateString
            },
            currentYear,
            currentMonth,
            currentDay
        )

        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    public fun validation() : Boolean{
        if(binding.tvCategory.text.trim().isEmpty()){
            Toast.makeText(this,"Please Enter Category",Toast.LENGTH_LONG).show()
            return false
        } else if(binding.tvCategory.text.trim().isNotEmpty() && binding.tvAddCategory.isVisible){
            Toast.makeText(this,"Please Add Category",Toast.LENGTH_LONG).show()
            return false
        }else if(binding.etNote.text.trim().toString().isEmpty()){
            Toast.makeText(this,"Please Enter Note",Toast.LENGTH_LONG).show()
            return false
        }else if(binding.tvSelectDate.text.trim().toString().isEmpty()){
            Toast.makeText(this,"Please Select Date",Toast.LENGTH_LONG).show()
            return false
        }else if(binding.etAmount.text.trim().toString().isEmpty() || binding.etAmount.text.trim().toString() == "0.0"){
            Toast.makeText(this,"Please Enter Amount",Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

}