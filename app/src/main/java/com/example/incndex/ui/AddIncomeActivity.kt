package com.example.incndex.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.incndex.R
import com.example.incndex.data.Amount
import com.example.incndex.data.Category
import com.example.incndex.data.Note
import com.example.incndex.data.PaymentResponse
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityAddIncomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode


public class AddIncomeActivity : AppCompatActivity(),PaymentAdapter.onClickListner {
    private lateinit var binding: ActivityAddIncomeBinding
    lateinit var userDao : UserDao
    var arrayList = ArrayList<String>()
    var arrayListOfNote = ArrayList<String>()
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
        val layoutManager = GridLayoutManager(this, 4)
        binding.rcPaymentType.setLayoutManager(layoutManager)

        listOfPayment.add(PaymentResponse("Card",R.drawable.card,false))
        listOfPayment.add(PaymentResponse("Online",R.drawable.netbanking,false))
        listOfPayment.add(PaymentResponse("Cash",R.drawable.money,true))
        listOfPayment.add(PaymentResponse("UPI",R.drawable.upi,false))


        paymentAdapter.paymentList = listOfPayment
        paymentAdapter.notifyDataSetChanged()

        binding.ivAdd.setBackgroundResource(R.drawable.ic_add);
        userDao = UserDatabase.getDatabase(applicationContext).userDao()

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
                if(i.isIncome){
                    listOfIncome.add(i)
                }
            }

//            if(listOfIncome.size > 0){
//                binding.ivList.visibility = View.VISIBLE
//            }
        }



        binding.tvNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()

                val filteredItems : List<String> = arrayListOfNote.filter { it.contains(inputText, true) }

                var adapter = ArrayAdapter(this@AddIncomeActivity, android.R.layout.simple_dropdown_item_1line, filteredItems)

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

                val filteredItems : List<String> = arrayList.filter { it.contains(inputText, true) }

                var adapter = ArrayAdapter(this@AddIncomeActivity, android.R.layout.simple_dropdown_item_1line, filteredItems)

                binding.tvCategory.threshold = 1;//will start working from first character
                binding.tvCategory.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView

            }
        })

        binding.tvSubmit.setOnClickListener {
            if (validation()) {
                CoroutineScope(Dispatchers.IO).launch {
                    if(listOfPayment.size > 0 && paymentMode.isNullOrEmpty()) paymentMode = listOfPayment[2].name
                    val amount = Amount(
                        name = binding.tvNote.text.trim().toString(),
                        category = binding.tvCategory.text.trim().toString(),
                        price = storeTwoDecimalNumber(binding.etAmount.text.trim().toString().toDouble()),
                        date = System.currentTimeMillis(),
                        payment_mode = paymentMode,
                        ref_id = 0,
                        isIncome = true)
                    userDao.addAmount(amount)

                    checkAndAddNote()
                    var isCategoryAvailable : Boolean = false
                    for(i in userDao.readCategory()){
                        if(i.name == binding.tvCategory.text.toString()){
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


                    var isNoteAvailable : Boolean = false
                    for(i in userDao.readNote()){
                        if(i.name == binding.tvNote.text.toString()){
                            isNoteAvailable = true
                            break
                        }
                    }
                    if(!isNoteAvailable) {
                        userDao.addNote(
                            Note(
                                name = binding.tvNote.text.trim().toString()
                            )
                        )
                        arrayList.add(binding.tvNote.text.trim().toString())
                    }


                    this@AddIncomeActivity.finish()

                }
            }
        }


        binding.ivList.setOnClickListener {
            var intent =
                Intent(this@AddIncomeActivity, ListOfIncomeActivity::class.java)
            startActivity(intent)
        }

    }
    fun checkAndAddNote(){
        var isNoteAvailable : Boolean = false
        for(i in userDao.readNote()){
            if(i.name.equals(binding.tvNote.text.toString())){
                isNoteAvailable = true
                break
            }
        }
        if(!isNoteAvailable) {
            userDao.addNote(
                Note(
                    name = binding.tvNote.text.trim().toString()
                )
            )
            arrayListOfNote.add(binding.tvNote.text.trim().toString())
        }
    }

    public fun validation() : Boolean{
        if(binding.tvCategory.text.trim().isEmpty()){
            Toast.makeText(this,"Please Enter Category",Toast.LENGTH_LONG).show()
            return false
        } else if(binding.tvNote.text.trim().toString().isEmpty()){
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

    fun storeTwoDecimalNumber(value: Double): Double {
        val decimalValue = BigDecimal(value)
            .setScale(2, RoundingMode.HALF_UP)

        return decimalValue.toDouble()
    }
}