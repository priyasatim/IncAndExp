package com.example.incndex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Income::class,Expenses::class,Category::class],version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao() : UserDao

    companion object {

        @Volatile
        private var INSTANCE : UserDatabase? = null

        fun getDatabase(context : Context) : UserDatabase {


            synchronized(this){

                var tempInstance : UserDatabase? = INSTANCE

                if(tempInstance == null){
                    tempInstance = Room.databaseBuilder(
                        context.applicationContext,
                        UserDatabase::class.java,
                        "user_database"
                    )
                        .build()
                }

            return tempInstance
            }
        }
    }
}