package com.example.incndex.ui

import android.R
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incndex.data.Amount
import com.example.incndex.data.RestoreData
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityListOfIncomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable


class ListOfIncomeActivity : AppCompatActivity(), IncomeAdapter.onClickListner,ListBottomSheetFragment.DataPassListener {
    private lateinit var adapter: IncomeAdapter
    private lateinit var binding: ActivityListOfIncomeBinding
    lateinit var userDao : UserDao
    private var itemList: ArrayList<Amount> = ArrayList()
    private var alertDialog: AlertDialog? = null
    var reversedList: ArrayList<Amount> = ArrayList()
    var listParentId = ArrayList<Amount>()
    var category : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListOfIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDatabase.getDatabase(applicationContext).userDao()

        binding.recyclevieiw.layoutManager = LinearLayoutManager(this)
        adapter = IncomeAdapter(this,this)
        binding.recyclevieiw.adapter = adapter

        CoroutineScope(Dispatchers.IO).launch {
            for (i in userDao.readAmount(null, null)) {
                if (i.isIncome) {
                    itemList.add(i)
                }
            }

            if(itemList.isEmpty()) {
                binding.ivNoDataFound.visibility = (if (adapter.itemCount == 0) View.VISIBLE else View.GONE)
                binding.ivDelete.visibility = (if (adapter.itemCount == 0) View.GONE else View.VISIBLE)
                binding.recyclevieiw.visibility = (if (adapter.itemCount == 0) View.GONE else View.VISIBLE)
                binding.searchIncome.visibility = (if (adapter.itemCount == 0) View.GONE else View.VISIBLE)
            } else {
                reversedList.addAll(itemList.reversed())

                withContext(Dispatchers.Main) {
                    adapter.updateItemList(reversedList, "",false)
                }
            }
        }

        binding.searchIncome.onActionViewExpanded();
        binding.searchIncome.clearFocus();
        binding.searchIncome.queryHint = "Search Note"

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
                adapter.updateItemList(reversedList, query,false)
                adapter.notifyDataSetChanged()
                binding.ivNoDataFound.visibility = (if (adapter.filteredItemList.isEmpty()) View.VISIBLE else View.GONE)
                binding.recyclevieiw.visibility = (if (adapter.filteredItemList.isEmpty()) View.GONE else View.VISIBLE)

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.updateItemList(reversedList, newText,false)
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

        binding.ivFilter.setOnClickListener {
            val bottomSheetFragment = ListBottomSheetFragment.newInstance(category)
            bottomSheetFragment.dataPassListener = this
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.ivRestore.setOnClickListener {
            if(listParentId.size == 0) {
                Toast.makeText(this,"Please select item",Toast.LENGTH_LONG).show()

            } else if(listParentId.size == 1){
                var intent = Intent(this, AddExpensesActivity::class.java)
                intent.putExtra("id", listParentId.get(0).id)
                intent.putExtra("price", listParentId.get(0).price)
                intent.putExtra("restore", true)
                intent.putExtra("isIncome", true)
                startActivity(intent)
            }  else {
                Toast.makeText(this,"Only one transaction allowed",Toast.LENGTH_LONG).show()

            }
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
                            }
                        }
                    }
                            if (childRefId.size == 0) {
                                userDao.delete(listParentId)

                            }else {
                                userDao.deleteRefId(childRefId)
                                userDao.delete(listParentId)
                            }

                }

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@ListOfIncomeActivity,"Data Deleted Successfully",Toast.LENGTH_LONG).show()

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

    override fun onSelect(item : Amount,isChecked : Boolean) {
        if(isChecked){
            listParentId.add(item)
        }
        else
        {
            if(!listParentId.isNullOrEmpty()){
                for(i in listParentId.withIndex()){
                    if(item.id == i.value.id) {
                        listParentId.remove(item)
                        break

                    }
                }
            }

        }
    }



    override fun onBalance(income: Amount) {
        val intent = Intent(this@ListOfIncomeActivity, RestoreHistoryActivity::class.java)
        intent.putExtra("name", income.name)
        intent.putExtra("category", income.category)
        startActivity(intent)
    }

    override fun onDataPassed(name: String) {
        category = name
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                adapter.updateItemList(reversedList, name,true)
                adapter.notifyDataSetChanged()

            }
        }
    }

}
