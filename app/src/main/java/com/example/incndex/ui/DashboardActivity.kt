package com.example.incndex.ui

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.incndex.R
import com.example.incndex.RoomDatabaseExporter
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityDashboardBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.net.URLDecoder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    lateinit var userDao: UserDao
    lateinit var database: UserDatabase

    var totalIncome: Double = 0.0
    var totalExpenses: Double = 0.0
    var total: Double = 0.0
    var isConvert: Boolean = false

    private val EXTERNAL_STORAGE_PERMISSION_CODE = 23
    lateinit var file: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDatabase.getDatabase(applicationContext).userDao()
        database = UserDatabase.getDatabase(applicationContext)


        CoroutineScope(Dispatchers.IO).launch {
            if (userDao.readAmount(null,null).isNotEmpty()) {
                for (i in userDao.readAmount(null,null)) {
                    if(i.isIncome){
                        totalIncome += i.price
                    } else
                        totalExpenses += i.price
                }

                binding.tvExpenses.text = totalExpenses.toString()

                if (totalIncome > totalExpenses) {
                    total = totalIncome - totalExpenses
                    binding.tvIncome.text = total.toString()
                } else
                    binding.tvIncome.text = totalIncome.toString()
            }
        }

        binding.llIncome.setOnClickListener {
            var intent = Intent(this, AddIncomeActivity::class.java)
            startActivity(intent)
        }

        binding.llExpenses.setOnClickListener {
            var intent = Intent(this, AddExpensesActivity::class.java)
            startActivity(intent)
        }

        binding.tvK.setOnClickListener {
            if (!isConvert) {
                isConvert = true
                if (totalIncome > totalExpenses) {
                    total = totalIncome - totalExpenses
                    binding.tvIncome.text = withSuffix(total)
                } else
                    binding.tvIncome.text = withSuffix(totalIncome)

                binding.tvExpenses.text = withSuffix(totalExpenses)
            } else {
                isConvert = false
                if (totalIncome > totalExpenses) {
                    total = totalIncome - totalExpenses
                    binding.tvIncome.text = total.toString()
                } else
                    binding.tvIncome.text = totalIncome.toString()

                binding.tvExpenses.text = totalExpenses.toString()
            }
        }

        binding.tvExcel.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {

                val builder = MaterialDatePicker.Builder.dateRangePicker()
                builder.setTheme(R.style.MyDatePickerDialogTheme)
                val datePicker = builder.build()

                datePicker.addOnPositiveButtonClickListener { selection ->
                    val calendarstart = Calendar.getInstance()
                    calendarstart.timeInMillis = selection.first ?: 0
                    calendarstart.set(Calendar.HOUR_OF_DAY, 0)
                    calendarstart.set(Calendar.MINUTE, 0)
                    calendarstart.set(Calendar.SECOND, 0)
                    val startDateInMillis = calendarstart.timeInMillis

                    val calendar = Calendar.getInstance()

                    calendar.timeInMillis = selection.second ?: 0
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)

                    val endDateInMillis = calendar.timeInMillis


                    CoroutineScope(Dispatchers.IO).launch {
                        requestExternalStoragePermission(startDateInMillis,endDateInMillis)
                    }
                    // Use the fromDate and toDate as needed
                }

                datePicker.show(supportFragmentManager, "datePicker")

            }
        }
    }

    private fun requestExternalStoragePermission(startDate : Long, endDate: Long) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            RoomDatabaseExporter.exportDatabaseToCsv(this@DashboardActivity,startDate,endDate)?.let {
                file = it
                val fileURL = try {
                    file.toURI().toURL().toString()
                } catch (e: URISyntaxException) {
                    val decodedPath = URLDecoder.decode(file.path, "UTF-8")
                    val uri = URI(decodedPath)
                    uri.toURL().toString()
                }
                val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
                downloadAndOpenFile(this,timeStamp + ".csv")

            }
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                EXTERNAL_STORAGE_PERMISSION_CODE
            )
        }
    }

    fun withSuffix(number: Double): String? {
        var numberString = ""
        val format = DecimalFormat("0.#")

        numberString = if (Math.abs(number / 1000000) > 1) {
            format.format((number / 1000000)).toString() + "M"
        } else if (Math.abs(number / 100000) > 1) {
            format.format((number / 100000)).toString() + "L"
        }else if (Math.abs(number / 1000) > 1) {
            format.format((number / 1000)).toString() + "K"
        }else if (Math.abs(number / 100) > 1) {
            format.format((number / 100)).toString() + "H"
        }else if (Math.abs(number / 10) > 1) {
            format.format((number / 10)).toString() + "T"
        } else {
            number.toString()
        }
        return numberString
    }

    override fun onRestart() {
        super.onRestart()

        CoroutineScope(Dispatchers.IO).launch {
            totalIncome = 0.0
            totalExpenses = 0.0

            if (userDao.readAmount(null,null).isNotEmpty()) {
                for (i in userDao.readAmount(null,null)) {
                    if(i.isIncome){
                        totalIncome += i.price
                    } else
                        totalExpenses += i.price
                }
                binding.tvExpenses.text = totalExpenses.toString()

                if (totalIncome > totalExpenses) {
                    total = totalIncome - totalExpenses
                    binding.tvIncome.text = total.toString()
                } else
                    binding.tvIncome.text = totalIncome.toString()
            }

            withContext(Dispatchers.Main) {
                binding.tvExpenses.text = totalExpenses.toString()


                if (totalIncome > totalExpenses) {
                    val total = totalIncome - totalExpenses
                    binding.tvIncome.text = total.toString()
                }
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // Add your code here to handle the granted permission
            } else {
                // Permission denied
                // Add your code here to handle the denied permission
            }
        }
    }

    fun downloadAndOpenFile(context: Context,  fileName: String) {
        createNotificationChannel(context)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Download in progress")
            .setContentText(fileName)
            .setSmallIcon(R.drawable.jt)
            .setProgress(0, 0, true)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

        GlobalScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {
                notificationManager.cancel(NOTIFICATION_ID)

                if (file != null) {
                    openDownloadedFile(context, file.path)
                } else {
                    // Handle download failure
                }
            }
        }
    }
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Download Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun openDownloadedFile(context: Context, filePath: String) {
        val file = File(filePath)
        val contentUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          FileProvider.getUriForFile(context, "your.package.name.fileprovider", file)

        } else {
            Uri.fromFile(file)
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setDataAndType(contentUri, "text/csv")

        var pendingIntent: PendingIntent? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_IMMUTABLE)

        } else {
            pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }



        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Download Complete")
            .setContentText(file.name)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.jt)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
              return
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

    }

    fun openCSVFile(filePath: String): List<List<String>> {
        val lines: MutableList<List<String>> = mutableListOf()

        try {
            val file = File(filePath)
            val reader = BufferedReader(FileReader(file))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val data = line!!.split(",")
                lines.add(data)
            }

            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return lines
    }

    companion object{

        private const val CHANNEL_ID = "DownloadChannel"
        private const val NOTIFICATION_ID = 1
    }




    }
