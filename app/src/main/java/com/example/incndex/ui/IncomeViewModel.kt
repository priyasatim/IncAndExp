package com.example.incndex.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class IncomeViewModel(private val incomeRepository: IncomeRepository) : ViewModel() {
//    private val _incomes = MutableLiveData<List<Income>>()
//    val incomes: LiveData<List<Income>> get() = _incomes
//
//    init {
//        loadIncomes()
//    }
//
//    private fun loadIncomes() {
//        viewModelScope.launch {
//            val incomeList = incomeRepository.getIncomes()
//            _incomes.value = incomeList
//        }
//    }
}