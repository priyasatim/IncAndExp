package com.example.incndex.data

import android.content.Context
import android.os.Environment
import androidx.room.Room
import com.opencsv.CSVReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date


public class RoomDatabaseExporter {

    companion object {
        fun exportDatabaseToCsv(context: Context, fromDate : Long, toDate : Long) : File? {
            var file : File? = null
            val db = Room.databaseBuilder(context, UserDatabase::class.java, "user_database").build()

            try {
                // Retrieve data from Room database
                val dataList = db.userDao().readAmount(fromDate,toDate)
                val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())

                // Create a file in the application's private directory
                file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "$timeStamp.csv"
                )
                val outputStream = FileOutputStream(file)
                val writer = OutputStreamWriter(outputStream)

                // Write header to CSV file
                val header = "Id,Date,Category,Expenses,Ref,Credit,Debit,Payment Mode" // Replace with your actual column names
                writer.write(header)
                writer.write("\n")

                // Write data rows to CSV file
                for (data in dataList) {
                    var rowData : String = ""

                    if(data.isIncome){
                         rowData = "${data.id},${convertLongToTime(data.date)},${data.category},${data.name},${data.ref_id},${data.price},${" "},${data.payment_mode}" // Replace with your actual column values
                    } else
                        rowData = "${data.id},${convertLongToTime(data.date)},${data.category},${data.name},${data.ref_id},${""},${data.price},${data.payment_mode}" // Replace with your actual column values

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

        fun convertLongToTime(time: Long): String {
            val date = Date(time)
            val format = SimpleDateFormat("dd.MM.yyyy")
            return format.format(date)
        }
    }
}