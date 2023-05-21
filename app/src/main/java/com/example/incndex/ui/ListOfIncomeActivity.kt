package com.example.incndex.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incndex.data.Income
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityListOfIncomeBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone


class ListOfIncomeActivity : AppCompatActivity() {
    private lateinit var adapter: IncomeAdapter
    private lateinit var binding: ActivityListOfIncomeBinding
    lateinit var userDao : UserDao
    private var itemList: List<Income> = emptyList()
    private var alertDialog: AlertDialog? = null
    var reversedList: List<Income> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListOfIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDatabase.getDatabase(applicationContext).userDao()

        binding.recyclevieiw.layoutManager = LinearLayoutManager(this)
        adapter = IncomeAdapter()
        binding.recyclevieiw.adapter = adapter

        CoroutineScope(Dispatchers.IO).launch {
            itemList = userDao.readIncome()
            reversedList = itemList.reversed()

            withContext(Dispatchers.Main) {
                adapter.updateItemList(reversedList, "")
            }
        }

        binding.searchIncome.onActionViewExpanded();
        binding.searchIncome.clearFocus();
        binding.searchIncome.queryHint = "Search"

        binding.imageviewDate.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.dateRangePicker().build()
                datePicker.show(supportFragmentManager, "DatePicker")

                datePicker.addOnPositiveButtonClickListener {

                    Toast.makeText(this, "${datePicker.headerText} is selected", Toast.LENGTH_LONG).show()
                }

        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkEmpty()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEmpty()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEmpty()
            }

            fun checkEmpty() {
                binding.ivNoDataFound.visibility = (if (adapter.itemCount == 0) View.VISIBLE else View.GONE)
                binding.ivDelete.visibility = (if (adapter.itemCount == 0) View.GONE else View.VISIBLE)
                binding.recyclevieiw.visibility = (if (adapter.itemCount == 0) View.GONE else View.VISIBLE)
            }
        })
        binding.searchIncome.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.updateItemList(reversedList, newText)
                return true
            }
        })

        val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                CoroutineScope(Dispatchers.IO).launch {
                    itemList = userDao.readIncome()
                    reversedList = itemList.reversed()
                    withContext(Dispatchers.Main) {
                        adapter.updateItemList(reversedList, "")
                    }
                }
            }
        }


        binding.flatIconAddIncome.setOnClickListener {
            var intent = Intent(this, AddIncomeActivity::class.java)
            intent.putExtra("isList",true)
            activityLauncher.launch(intent);

        }

        binding.ivDelete.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder?.setTitle("Confirmation")
        builder?.setMessage("Are you sure you want to delete all records?")

        // Set positive button and its click listener
        builder.setPositiveButton("Yes") { dialog: DialogInterface, which: Int ->
            // User clicked "Yes", perform the desired action
            CoroutineScope(Dispatchers.IO).launch {
                val income = userDao.readIncome()
                reversedList = income.reversed()
                userDao.deleteIncome(reversedList)
                withContext(Dispatchers.Main) {
                    adapter.updateItemList(reversedList, "")
                    alertDialog?.dismiss()
                }
            }

        }

        // Set negative button and its click listener
        builder.setNegativeButton("No") { dialog: DialogInterface, which: Int ->
            // User clicked "No" or dismissed the dialog, handle accordingly
            alertDialog?.dismiss()

        }

        alertDialog = builder.create()
        alertDialog?.show()
    }

}