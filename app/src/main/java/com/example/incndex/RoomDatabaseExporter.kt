package com.example.incndex

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.room.Room
import com.example.incndex.data.Amount
import com.example.incndex.data.Income
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.opencsv.CSVReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.OutputStreamWriter


public class RoomDatabaseExporter {

    companion object {
        fun exportDatabaseToCsv(context: Context, fromDate : Long, toDate : Long) : File? {
            var file : File? = null
            val db = Room.databaseBuilder(context, UserDatabase::class.java, "user_database").build()

            try {
                // Retrieve data from Room database
                val dataList = db.userDao().selectIncome()



                // Create a file in the application's private directory
                file = File(context.filesDir, "priya.csv")
                val outputStream = FileOutputStream(file)
                val writer = OutputStreamWriter(outputStream)

                // Write header to CSV file
                val header = "Id,Date,Expenses,Ref,Credit,Debit" // Replace with your actual column names
                writer.write(header)
                writer.write("\n")

                // Write data rows to CSV file
                for (data in dataList) {
                    val rowData = "${data.id},${data.date},${data.name},${1},${data.price},${data.price}" // Replace with your actual column values
                    writer.write(rowData)
                    writer.write("\n")
                }


                    val csvReader = CSVReader(FileReader(file))
                    var record: Array<String>?

                    while (csvReader.readNext().also { record = it } != null) {
                        // Process each record in the CSV file
                        for (value in record!!) {
                            println(value)
                        }
                    }

                    csvReader.close()

                writer.flush()

            } catch (e: IOException) {
                e.printStackTrace()
                // Handle the exception as per your requirement
            } finally {
                db.close()
            }
            return file
        }
    }
}