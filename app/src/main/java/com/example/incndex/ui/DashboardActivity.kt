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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.incndex.RoomDatabaseExporter
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityDashboardBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.URL
import java.util.Calendar


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

    private var fromDateCalendar: Calendar = Calendar.getInstance()
    private var toDateCalendar: Calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = UserDatabase.getDatabase(applicationContext).userDao()
        database = UserDatabase.getDatabase(applicationContext)


        CoroutineScope(Dispatchers.IO).launch {
            if (userDao.readIncome().isNotEmpty()) {
                for (i in userDao.readIncome()) {
                    totalIncome += i.price
                }
            }

            if (userDao.readExpenses().isNotEmpty()) {
                for (i in userDao.readExpenses()) {
                    totalExpenses += i.price
                }
                binding.tvExpenses.text = totalExpenses.toString()
            }

            if (totalIncome > totalExpenses) {
                total = totalIncome - totalExpenses
                binding.tvIncome.text = total.toString()
            } else
                binding.tvIncome.text = totalIncome.toString()


        }

        binding.tvIncome.setOnClickListener {
            var intent = Intent(this, AddIncomeActivity::class.java)
            startActivity(intent)
        }

        binding.tvExpenses.setOnClickListener {
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
                val datePicker = builder.build()

                datePicker.addOnPositiveButtonClickListener { selection ->
                    val startTimestamp = selection.first
                    val endTimestamp = selection.second

                    val startDate = Calendar.getInstance()
                    startDate.timeInMillis = startTimestamp

                    val endDate = Calendar.getInstance()
                    endDate.timeInMillis = endTimestamp

                    fromDateCalendar = startDate
                    toDateCalendar = endDate

                    CoroutineScope(Dispatchers.IO).launch {
                        requestExternalStoragePermission()
                    }
                    // Use the fromDate and toDate as needed
                }

                datePicker.show(supportFragmentManager, "datePicker")

            }
        }
    }

    private fun requestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            RoomDatabaseExporter.exportDatabaseToCsv(this@DashboardActivity,fromDateCalendar.timeInMillis,toDateCalendar.timeInMillis)?.let {
                file = it

                val sourceFilePath = it.absolutePath
                val destinationFilePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/priya.csv"

                val fileName = "priya.csv"
                showDownloadNotification(this,file,fileName)
//                downloadFile(this, destinationFilePath, fileName)
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

    fun withSuffix(count: Double): String? {
        if (count < 10) return "" + count
        val exp = (Math.log(count.toDouble()) / Math.log(2.0)).toInt()
        return String.format(
            "%.1f %c",
            count / Math.pow(2.0, exp.toDouble()),
            "THkLCA"[exp - 1]
        )
    }

    override fun onRestart() {
        super.onRestart()

        CoroutineScope(Dispatchers.IO).launch {
            totalIncome = 0.0
            totalExpenses = 0.0

            if (userDao.readIncome().isNotEmpty()) {
                for (i in userDao.readIncome()) {
                    totalIncome += i.price
                }
            }

            if (userDao.readExpenses().isNotEmpty()) {
                for (i in userDao.readExpenses()) {
                    totalExpenses += i.price
                }
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
    fun copyFile(context: Context, sourcePath: String, destinationPath: String): Boolean {
        return try {
            val sourceFile = File(sourcePath)
            val destinationFile = File(destinationPath)

            val sourceStream = FileInputStream(sourceFile)
            val destinationStream = FileOutputStream(destinationFile)

            val buffer = ByteArray(1024)
            var bytesRead = sourceStream.read(buffer)
            while (bytesRead != -1) {
                destinationStream.write(buffer, 0, bytesRead)
                bytesRead = sourceStream.read(buffer)
            }

            destinationStream.flush()
            destinationStream.close()
            sourceStream.close()

            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun downloadFile(context: Context, filePath: String, fileName: String) {
        val fileUri = Uri.fromFile(File(filePath))

        val request = DownloadManager.Request(fileUri)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(fileName)
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    fun showDownloadNotification(context: Context, file: File, fileName: String) {
        val channelId = "download_channel"
        val notificationId = 123

        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Download"
            val channelDescription = "File Download Notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableLights(true)
                lightColor = Color.BLUE
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent to open the downloaded file
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        intent.setDataAndType(fileUri, "text/csv")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Downloading $fileName")
            .setContentText("Download in progress")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

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
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

}
