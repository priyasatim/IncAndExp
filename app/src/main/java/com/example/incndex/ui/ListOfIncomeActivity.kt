package com.example.incndex.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incndex.data.Amount
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityListOfIncomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListOfIncomeActivity : AppCompatActivity(), IncomeAdapter.onClickListner {
    private lateinit var adapter: IncomeAdapter
    private lateinit var binding: ActivityListOfIncomeBinding
    lateinit var userDao : UserDao
    private var itemList: ArrayList<Amount> = ArrayList()
    private var alertDialog: AlertDialog? = null
    var reversedList: ArrayList<Amount> = ArrayList()
    var listParentId = ArrayList<Amount>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListOfIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDatabase.getDatabase(applicationContext).userDao()

        binding.recyclevieiw.layoutManager = LinearLayoutManager(this)
        adapter = IncomeAdapter(this)
        binding.recyclevieiw.adapter = adapter


        CoroutineScope(Dispatchers.IO).launch {
            for(i in userDao.readAmount(null,null))
            {
                if(i.isIncome){
                    itemList.add(i)
                }
            }
            reversedList.addAll(itemList.reversed())

            withContext(Dispatchers.Main) {
                    adapter.updateItemList(reversedList, "")
            }
        }

        binding.searchIncome.onActionViewExpanded();
        binding.searchIncome.clearFocus();
        binding.searchIncome.queryHint = "Search"

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
                adapter.updateItemList(reversedList, query)
                adapter.notifyDataSetChanged()
                binding.ivNoDataFound.visibility = (if (adapter.filteredItemList.isEmpty()) View.VISIBLE else View.GONE)
                binding.recyclevieiw.visibility = (if (adapter.filteredItemList.isEmpty()) View.GONE else View.VISIBLE)

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.updateItemList(reversedList, newText)
                adapter.notifyDataSetChanged()
                binding.ivNoDataFound.visibility = (if (adapter.filteredItemList.isEmpty()) View.VISIBLE else View.GONE)
                binding.recyclevieiw.visibility = (if (adapter.filteredItemList.isEmpty()) View.GONE else View.VISIBLE)

                return true
            }
        })

        binding.ivDelete.setOnClickListener {
            if(listParentId.isEmpty()){
                Toast.makeText(this,"Please select item",Toast.LENGTH_LONG).show()
            } else
            showConfirmationDialog()
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder?.setTitle("Confirmation")
        builder?.setMessage("Are you sure you want to delete this records?")

        // Set positive button and its click listener
        builder.setPositiveButton("Yes") { dialog: DialogInterface, which: Int ->

                var childRefId = ArrayList<Amount>()

                CoroutineScope(Dispatchers.IO).launch {
                    for(k in listParentId) {
                        for (i in userDao.readAmount(null, null)) {
                            if (k.id == i.ref_id) {
                                childRefId.add(i)
                                userDao.deleteRefId(childRefId)
                                userDao.deleteParent(k)

                            }
                        }
                    }
                    if(childRefId.size == 0) {
                        userDao.delete(listParentId)
                    }

                    this@ListOfIncomeActivity.finish()
                    var intent = Intent(this@ListOfIncomeActivity, DashboardActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.removeFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
                alertDialog?.dismiss()
            }


        // Set negative button and its click listener
        builder.setNegativeButton("No") { dialog: DialogInterface, which: Int ->
            // User clicked "No" or dismissed the dialog, handle accordingly
            alertDialog?.dismiss()

        }

        alertDialog = builder.create()
        alertDialog?.show()
    }

    override fun onDelete(list: List<Amount>) {
        listParentId.clear()
        listParentId.addAll(list)
    }

    override fun onRestore(income: Amount) {
        var intent = Intent(this, AddExpensesActivity::class.java)
        intent.putExtra("id",income.id)
        intent.putExtra("price",income.price)
        intent.putExtra("restore",true)
        intent.putExtra("isIncome",true)
        Log.d("ListOfIncomeActivity",""+income.id + " "+ income.price)
        startActivity(intent)
    }

}
