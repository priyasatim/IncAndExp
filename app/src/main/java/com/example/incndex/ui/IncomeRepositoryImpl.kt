package com.example.incndex.ui

import com.example.incndex.data.UserDao

class IncomeRepositoryImpl(private val incomeDao: UserDao) : IncomeRepository {
//    override suspend fun getIncomes(): List<Income> {
//        return incomeDao.readIncome(null,null)
//    }
}