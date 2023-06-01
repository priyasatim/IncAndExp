package com.example.incndex.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

public class AddIncomeActivity : AppCompatActivity(),PaymentAdapter.onClickListner {
    private lateinit var binding: ActivityAddIncomeBinding
    lateinit var userDao : UserDao
    var arrayList = ArrayList<String>()
    private lateinit var paymentAdapter : PaymentAdapter
    var listOfPayment : ArrayList<PaymentResponse> = arrayListOf()
    var paymentMode : String = ""
    var listOfIncome : ArrayList<Amount> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rcPaymentType.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        paymentAdapter = PaymentAdapter(this,this)
        binding.rcPaymentType.adapter = paymentAdapter

        listOfPayment.add(PaymentResponse("Cash",R.drawable.money,false))
        listOfPayment.add(PaymentResponse("Card",R.drawable.card,false))
        listOfPayment.add(PaymentResponse("UPI",R.drawable.upi,false))
        listOfPayment.add(PaymentResponse("Netbanking",R.drawable.netbanking,false))

        paymentAdapter.paymentList = listOfPayment
        paymentAdapter.notifyDataSetChanged()

        binding.ivAdd.setBackgroundResource(R.drawable.ic_add);
        userDao = UserDatabase.getDatabase(applicationContext).userDao()

        CoroutineScope(Dispatchers.IO).launch {
            if (userDao.readCategory().isNotEmpty()) {
                for (i in userDao.readCategory()) {
                    arrayList.add(i.name)
                }
            }

            for (i in userDao.readAmount(null,null)) {
                if(i.isIncome){
                    listOfIncome.add(i)
                }
            }

            if(listOfIncome.size > 0){
                binding.ivList.visibility = View.VISIBLE
            }
        }



        binding.tvCategory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()

                val filteredItems : List<String> = arrayList.filter { it.contains(inputText, true) }

                var adapter = ArrayAdapter(this@AddIncomeActivity, android.R.layout.simple_dropdown_item_1line, filteredItems)

                binding.tvCategory.threshold = 1;//will start working from first character
                binding.tvCategory.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView

            }
        })


        binding.tvSubmit.setOnClickListener {
            if (validation()) {
                CoroutineScope(Dispatchers.IO).launch {
                    if(listOfPayment.size > 0 && paymentMode.isNullOrEmpty()) paymentMode = listOfPayment[0].name
                    val amount = Amount(
                        name = binding.etNote.text.trim().toString(),
                        category = binding.tvCategory.text.trim().toString(),
                        price = binding.etAmount.text.trim().toString().toDouble(),
                        date = System.currentTimeMillis(),
                        payment_mode = paymentMode,
                        ref_id = 0,
                        isIncome = true
                    )
                    userDao.addAmount(amount)
                    userDao.addCategory(Category(name = binding.tvCategory.text.trim().toString()))
                    arrayList.add(binding.tvCategory.text.trim().toString())


                    this@AddIncomeActivity.finish()
                    var intent =
                        Intent(this@AddIncomeActivity, ListOfIncomeActivity::class.java)
                    startActivity(intent)

                }
            }
        }

        binding.ivList.setOnClickListener {
            var intent =
                Intent(this@AddIncomeActivity, ListOfIncomeActivity::class.java)
            startActivity(intent)
        }

    }

    public fun validation() : Boolean{
        if(binding.tvCategory.text.trim().isEmpty()){
            Toast.makeText(this,"Please Enter Category",Toast.LENGTH_LONG).show()
            return false
        } else if(binding.etNote.text.trim().toString().isEmpty()){
            Toast.makeText(this,"Please Enter Note",Toast.LENGTH_LONG).show()
            return false
        }else if(binding.etAmount.text.trim().toString().isEmpty() || binding.etAmount.text.trim().toString() == "0.0"){
            Toast.makeText(this,"Please Enter Amount",Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
    override fun onItemClick(position: Int) {
        paymentMode = listOfPayment[position].name
    }
}