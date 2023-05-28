package com.example.incndex.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income_table")
data class Income(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    @ColumnInfo(name = "name")  val name : String,
    @ColumnInfo(name = "category")  val category : String,
    @ColumnInfo(name = "price")  val price : Double,
    @ColumnInfo(name = "date")  val date : String,
    @ColumnInfo(name = "payment_mode")  val payment_mode : String,
     var isSelect : Boolean,
) {
}
