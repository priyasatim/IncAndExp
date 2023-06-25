package com.example.incndex.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface UserDao {

 @Insert(onConflict = OnConflictStrategy.IGNORE)
 fun addAmount(amount : Amount)

 @Insert(onConflict = OnConflictStrategy.IGNORE)
 fun addCategory(category : Category)

 @Query("SELECT * FROM amount_table WHERE (:startDate IS NULL OR date >= :startDate) AND (:endDate IS NULL OR date <= :endDate)")
 fun readAmount(startDate: Long?, endDate: Long?) : List<Amount>

 @Query("SELECT * FROM category")
 fun readCategory() : List<Category>

@Delete
fun deleteRefId(refIds: ArrayList<Amount>)
 @Delete
 fun deleteParent(parentId: Amount)

 @Delete
 fun delete(ids: ArrayList<Amount>)

 @Query("DELETE FROM amount_table")
 fun deleteAll()

 @Insert
 fun insertAll(persons: List<Amount>)
}