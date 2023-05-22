package com.example.incndex.ui

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.incndex.R
import com.example.incndex.data.Expenses
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityListOfExpensesBinding
import com.example.incndex.databinding.ActivityListOfIncomeBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListOfExpensesActivity : AppCompatActivity() {
    private lateinit var adapter: ExpensesAdapter
    private lateinit var binding: ActivityListOfExpensesBinding
    lateinit var userDao : UserDao
    private var itemList: List<Expenses> = emptyList()
    private var alertDialog: AlertDialog? = null
    var reversedList: List<Expenses> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListOfExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            this.onBackPressed()
        }

        userDao = UserDatabase.getDatabase(applicationContext).userDao()

        binding.recyclevieiw.layoutManager = LinearLayoutManager(this)
        adapter = ExpensesAdapter()
        binding.recyclevieiw.adapter = adapter
        binding.searchExpenses.onActionViewExpanded();
        binding.searchExpenses.clearFocus();
        binding.searchExpenses.queryHint = "Search"

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
//                binding.ivNoDataFound.visibility = (if (adapter.itemCount == 0) View.VISIBLE else View.GONE)
//                binding.ivDelete.visibility = (if (adapter.itemCount == 0) View.GONE else View.VISIBLE)
//                binding.recyclevieiw.visibility = (if (adapter.itemCount == 0) View.GONE else View.VISIBLE)
            }
        })


        binding.searchExpenses.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                adapter.updateItemList(reversedList, query)
                binding.ivNoDataFound.visibility = (if (adapter.filteredItemList.isEmpty()) View.VISIBLE else View.GONE)
                binding.recyclevieiw.visibility = (if (adapter.filteredItemList.isEmpty()) View.GONE else View.VISIBLE)

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.updateItemList(reversedList, newText)
                    binding.ivNoDataFound.visibility = (if (adapter.filteredItemList.isEmpty()) View.VISIBLE else View.GONE)
                    binding.recyclevieiw.visibility = (if (adapter.filteredItemList.isEmpty()) View.GONE else View.VISIBLE)

                return true
            }
        })

        CoroutineScope(Dispatchers.IO).launch {

            itemList = userDao.readExpenses()
            reversedList = itemList.reversed()
            withContext(Dispatchers.Main) {
                adapter.updateItemList(reversedList, "")
            }

        }

        val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                CoroutineScope(Dispatchers.IO).launch {
                    var itemList = userDao.readExpenses()
                    reversedList = itemList.reversed()
                    withContext(Dispatchers.Main) {
                        adapter.updateItemList(reversedList, "")
                    }
                }

            }
        }

        binding.flatIconAddExpenses.setOnClickListener {
            var intent = Intent(this, AddExpensesActivity::class.java)
            intent.putExtra("isList",true)
            activityLauncher.launch(intent)
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
                val expenses = userDao.readExpenses()
                reversedList = expenses.reversed()
                userDao.deleteExpenses(reversedList)
                withContext(Dispatchers.Main) {
                    adapter.updateItemList(reversedList, "")
                }
                alertDialog?.dismiss()
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