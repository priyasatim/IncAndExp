package com.example.incndex.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.incndex.R
import com.example.incndex.data.Amount
import com.example.incndex.data.Category
import com.example.incndex.data.PaymentResponse
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityAddIncomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class AddExpensesActivity : AppCompatActivity(),PaymentAdapter.onClickListner {
    private lateinit var binding: ActivityAddIncomeBinding
    var paymentMode : String = ""
    lateinit var userDao : UserDao
    var isComingFromList : Boolean = false
    var arrayList = ArrayList<String>()
    var arrayListOfNote = ArrayList<String>()
    var isRestore : Boolean = false
    var isIncome : Boolean = false
    var price : Double = 0.0
    var incomeAmount : Double = 0.0
    var id : Int = 0
    private lateinit var paymentAdapter : PaymentAdapter
    var listOfPayment : ArrayList<PaymentResponse> = arrayListOf()
    var listOfExpenses : ArrayList<Amount> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.getBooleanExtra("restore",false)){
            isRestore = true
            isIncome = intent.getBooleanExtra("isIncome",false)
            id = intent.getIntExtra("id",0)
            price = intent.getDoubleExtra("price",0.0)
            if(isIncome) binding.etAmount.setText(price.toString())
            if(isIncome)  binding.tvNote.hint = "Expense Note" else binding.tvNote.hint = "Saving Note"

        } else {
            incomeAmount = intent.getDoubleExtra("income_amount",0.0)
            binding.tvNote.hint = "Expense Note"
        }

        if(isRestore && !isIncome)
        binding.ivAdd.setBackgroundResource(R.drawable.ic_add);
        else
        binding.ivAdd.setBackgroundResource(R.drawable.ic_minus);

        binding.rcPaymentType.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,false)
        paymentAdapter = PaymentAdapter(this,this)
        binding.rcPaymentType.adapter = paymentAdapter
        val layoutManager = GridLayoutManager(this, 4)
        binding.rcPaymentType.setLayoutManager(layoutManager)

        listOfPayment.add(PaymentResponse("Card",R.drawable.card,false))
        listOfPayment.add(PaymentResponse("Online",R.drawable.netbanking,false))
        listOfPayment.add(PaymentResponse("Cash",R.drawable.money,true))
        listOfPayment.add(PaymentResponse("UPI",R.drawable.upi,false))


        paymentAdapter.paymentList = listOfPayment
        paymentAdapter.notifyDataSetChanged()

        userDao = UserDatabase.getDatabase(applicationContext).userDao()
        if (intent.getBooleanExtra("isList", false)) {
            isComingFromList = intent.getBooleanExtra("isList", false)
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (userDao.readNote().isNotEmpty()) {
                for (i in userDao.readNote()) {
                    arrayListOfNote.add(i.name)
                }
            }

            if (userDao.readCategory().isNotEmpty()) {
                for (i in userDao.readCategory()) {
                    arrayList.add(i.name)
                }
            }

            for (i in userDao.readAmount(null,null)) {
                if(!i.isIncome){
                    listOfExpenses.add(i)
                }
            }

            if(isRestore)
                binding.ivList.visibility = View.GONE
//            else if(listOfExpenses.size > 0 ){
//                binding.ivList.visibility = View.VISIBLE
//            }
        }

        binding.ivList.setOnClickListener {
            var intent =
                Intent(this@AddExpensesActivity, ListOfExpensesActivity::class.java)
            startActivity(intent)
        }

        binding.tvNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()

                val filteredItems : List<String> = arrayListOfNote.filter { it.contains(inputText, true) }

                var adapter = ArrayAdapter(this@AddExpensesActivity, android.R.layout.simple_dropdown_item_1line, filteredItems)

                binding.tvNote.threshold = 1;//will start working from first character
                binding.tvNote.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView

            }
        })
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
                    if(listOfPayment.size > 0 && paymentMode.isNullOrEmpty()) paymentMode = listOfPayment[2].name

                    var restoreId = 0
                    if(isRestore) restoreId = id
                    if(!isIncome && isRestore)
                    {
                        val amount = Amount(
                            name = binding.tvNote.text.trim().toString(),
                            category = binding.tvCategory.text.trim().toString(),
                            price = storeTwoDecimalNumber(binding.etAmount.text.trim().toString().toDouble()),
                            date = System.currentTimeMillis(),
                            payment_mode = paymentMode,
                            ref_id = restoreId,
                            isIncome = true
                        )
                        userDao.addAmount(amount)

                        checkAndAddCategory()
                        this@AddExpensesActivity.finish()
                        var intent =
                            Intent(this@AddExpensesActivity, DashboardActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)

                    } else {
                        val amount = Amount(
                            name = binding.tvNote.text.trim().toString(),
                            category = binding.tvCategory.text.trim().toString(),
                            price = binding.etAmount.text.trim().toString().toDouble(),
                            date = System.currentTimeMillis(),
                            payment_mode = paymentMode,
                            ref_id = restoreId,
                            isIncome = false
                        )
                        userDao.addAmount(amount)

                        checkAndAddCategory()

                        this@AddExpensesActivity.finish()
                        var intent =
                            Intent(this@AddExpensesActivity, DashboardActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    }

                }
            }
        }

    }

    fun checkAndAddCategory(){
        var isCategoryAvailable : Boolean = false
        for(i in userDao.readCategory()){
            if(i.name.equals(binding.tvCategory.text.toString())){
                isCategoryAvailable = true
                break
            }
        }
        if(!isCategoryAvailable) {
            userDao.addCategory(
                Category(
                    name = binding.tvCategory.text.trim().toString()
                )
            )
            arrayList.add(binding.tvCategory.text.trim().toString())
        }
    }

        public fun validation() : Boolean{
            if(binding.tvCategory.text.trim().isEmpty()){
                Toast.makeText(this,"Please Enter Category",Toast.LENGTH_LONG).show()
                return false
            }else if(binding.tvNote.text.trim().toString().isEmpty()){
                Toast.makeText(this,"Please Enter Note",Toast.LENGTH_LONG).show()
                return false
            }else if(binding.etAmount.text.trim().toString().isEmpty() || binding.etAmount.text.trim().toString() == "0.0"){
                Toast.makeText(this,"Please Enter Amount",Toast.LENGTH_LONG).show()
                return false
            } else if(isRestore && isIncome && binding.etAmount.text.toString().toDouble() > price){
                Toast.makeText(this,"Restore Amount should be less than income price",Toast.LENGTH_LONG).show()
                return false
            }else if(!isRestore && binding.etAmount.text.toString().toDouble() > incomeAmount){
                Toast.makeText(this,"Expenses Amount should be less than Saving",Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }

    override fun onItemClick(position: Int) {
        paymentMode = listOfPayment[position].name
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val parentActivityClass = DashboardActivity::class.java // Replace with the class of your parent activity

        val intent = Intent(this, parentActivityClass)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)

        finish()
    }

    fun storeTwoDecimalNumber(value: Double): Double {
        val decimalValue = BigDecimal(value)
            .setScale(2, RoundingMode.HALF_UP)

        return decimalValue.toDouble()
    }
}