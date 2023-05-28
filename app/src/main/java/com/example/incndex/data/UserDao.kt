package com.example.incndex.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.Date

@Dao
interface UserDao {

 @Insert(onConflict = OnConflictStrategy.IGNORE)
 fun addIncome(income : Income)

 @Query("SELECT * FROM income_table")
 fun readIncome() : List<Income>

 @Query("SELECT * FROM income_table")
 fun selectIncome() : List<Income>

 @Insert(onConflict = OnConflictStrategy.IGNORE)
 fun addExpenses(expenses : Expenses)

 @Query("SELECT * FROM expense_table")
 fun readExpenses() : List<Expenses>

 @Insert(onConflict = OnConflictStrategy.IGNORE)
 fun addCategory(category : Category)

 @Query("SELECT * FROM category")
 fun readCategory() : List<Category>

 @Delete
 fun deleteIncome(income: List<Income>)

 @Delete
 fun deleteExpenses(expenses: List<Expenses>)

}