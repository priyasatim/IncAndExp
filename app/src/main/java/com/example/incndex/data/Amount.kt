package com.example.incndex.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "amount_table")
data class Amount(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    @ColumnInfo(name = "name")  val name : String,
    @ColumnInfo(name = "from_date")  val from_date : String,
    @ColumnInfo(name = "to_date")  val to_date : String,
)
