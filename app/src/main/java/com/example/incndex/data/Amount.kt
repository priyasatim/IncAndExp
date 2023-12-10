package com.example.incndex.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "amount_table")
data class Amount(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    @ColumnInfo(name = "ref_id")  val ref_id : Int,
    @ColumnInfo(name = "name")  val name : String,
    @ColumnInfo(name = "category")  val category : String,
    @ColumnInfo(name = "price")  val price : Double,
    @ColumnInfo(name = "date")  val date : Long,
    @ColumnInfo(name = "payment_mode")  val payment_mode : String,
    @ColumnInfo(name = "is_income")  val isIncome : Boolean,
)




