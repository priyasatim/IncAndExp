package com.example.incndex.ui

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.incndex.R
import com.example.incndex.data.RoomDatabaseExporter
import com.example.incndex.data.Amount
import com.example.incndex.data.UserDao
import com.example.incndex.data.UserDatabase
import com.example.incndex.databinding.ActivityDashboardBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.opencsv.CSVReaderBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    lateinit var userDao: UserDao
    lateinit var database: UserDatabase

    var totalIncome: Double = 0.0
    var totalExpenses: Double = 0.0
    var isConverted: Boolean = false

    private val EXTERNAL_STORAGE_PERMISSION_CODE = 23
    private val PICK_EXCEL_FILE_REQUEST = 123
    lateinit var file: File
    private val READ_WRITE_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        userDao = UserDatabase.getDatabase(applicationContext).userDao()
        database = UserDatabase.getDatabase(applicationContext)
        isConverted = sharedPreferences.getBoolean("isFormat", false)

        binding.tvImportExcel.setOnClickListener {
            checkReadWritePermissions()
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (userDao.readAmount(null,null).isNotEmpty()) {

                for (i in userDao.readAmount(null, null)) {
                    if (i.ref_id == 0) {
                        if (i.isIncome) {
                            totalIncome += i.price
                        } else {
                            totalExpenses += i.price
                            totalIncome -= i.price

                        }
                    } else {
                        if (i.isIncome) {
                            totalExpenses -= i.price
                            totalIncome += i.price
                        } else {
                            totalIncome -= i.price
                            totalExpenses += i.price
                        }
                    }
                }




                if(isConverted)
                    binding.tvExpenses.text = withSuffix(totalExpenses)
                else
                    binding.tvExpenses.text = storeTwoDecimalNumber(totalExpenses).toString()

                if(isConverted)
                    binding.tvIncome.text = withSuffix(totalIncome)
                    else
                    binding.tvIncome.text = storeTwoDecimalNumber(totalIncome).toString()

                if(isConverted)
                    binding.tvK.setImageResource(R.drawable.number_format)
                else
                    binding.tvK.setImageResource(R.drawable.k_format)

            }
        }

        binding.llIncome.setOnClickListener {
            var intent = Intent(this, AddIncomeActivity::class.java)
            startActivity(intent)
        }

        binding.llExpenses.setOnClickListener {
            var intent = Intent(this, AddExpensesActivity::class.java)
            intent.putExtra("income_amount",totalIncome)
            startActivity(intent)
        }

        binding.tvK.setOnClickListener {
            if (!isConverted) {
                    binding.tvIncome.text = withSuffix(totalIncome)

                binding.tvExpenses.text = withSuffix(totalExpenses)
                val editor = sharedPreferences.edit()
                editor.putBoolean("isFormat", true);
                editor.commit();
                isConverted = true

                binding.tvK.setImageResource(R.drawable.number_format)

            } else {
                binding.tvIncome.text = storeTwoDecimalNumber(totalIncome).toString()

                binding.tvExpenses.text = storeTwoDecimalNumber(totalExpenses).toString()
                val editor = sharedPreferences.edit()
                editor.putBoolean("isFormat", false);
                editor.commit();
                isConverted = false

                binding.tvK.setImageResource(R.drawable.k_format)
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

        numberString = if (Math.abs(number / 1000000000) >= 1) {
            format.format((number / 1000000000)).toString() + "C"
        } else if (Math.abs(number / 100000) >= 1) {
            format.format((number / 100000)).toString() + "L"
        }else if (Math.abs(number / 1000) >= 1) {
            format.format((number / 1000)).toString() + "K"
        }else if (Math.abs(number / 100) >= 1) {
            format.format((number / 100)).toString() + "H"
        }else if (Math.abs(number / 10) >= 1) {
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

                for (i in userDao.readAmount(null, null)) {
                    if (i.ref_id == 0) {
                        if (i.isIncome) {
                            totalIncome += i.price
                        } else {
                            totalExpenses += i.price
                            totalIncome -= i.price                        }
                    } else {
                        if (i.isIncome) {
                            totalExpenses -= i.price
                            totalIncome += i.price
                        } else {
                            totalIncome -= i.price
                            totalExpenses += i.price
                        }
                    }
                }


                if(isConverted)
                    binding.tvExpenses.text = withSuffix(totalExpenses)
                else
                    binding.tvExpenses.text = storeTwoDecimalNumber(totalExpenses).toString()

                if(isConverted)
                    binding.tvIncome.text = withSuffix(totalIncome)
                else
                    binding.tvIncome.text = storeTwoDecimalNumber(totalIncome).toString()

                if(isConverted)
                    binding.tvK.setImageResource(R.drawable.number_format)
                else
                    binding.tvK.setImageResource(R.drawable.k_format)

            }


            withContext(Dispatchers.Main) {
                binding.tvExpenses.text = if(isConverted) withSuffix(totalExpenses) else totalExpenses.toString()

                   if(isConverted) binding.tvIncome.text = withSuffix(totalIncome) else binding.tvIncome.text = storeTwoDecimalNumber(totalIncome).toString()

                if(isConverted)
                    binding.tvK.setImageResource(R.drawable.number_format)
                else
                    binding.tvK.setImageResource(R.drawable.k_format)

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
        if (requestCode == READ_WRITE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, perform desired actions

            } else {
                // Permissions denied
                // Handle accordingly, show an explanation, or disable functionality
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

    companion object{

        private const val CHANNEL_ID = "DownloadChannel"
        private const val NOTIFICATION_ID = 1
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_EXCEL_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val uri = data?.data
                if (uri != null) {
                    val csvFile = getFileFromUri(uri)
                    if (csvFile != null) {
                        val fileName = uri?.let { getFileNameFromUri(it) }
                        if(fileName?.let { isCsvFile(it) } == true) {
                            importCsvToDatabase(this, csvFile)
                        } else {
                            Toast.makeText(this@DashboardActivity,"Please select csv file",Toast.LENGTH_LONG).show()

                        }
                        }
                }


            }
        }
    }



    private fun openDownloadFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, PICK_EXCEL_FILE_REQUEST)
    }

    private fun checkReadWritePermissions() {
        val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
        val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

        val hasReadPermission = ContextCompat.checkSelfPermission(this, readPermission) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(this, writePermission) == PackageManager.PERMISSION_GRANTED

        if (hasReadPermission && hasWritePermission) {
            // Permissions already granted, perform desired actions
            openDownloadFolder()

        } else {
            // Request permissions
            requestPermissions(arrayOf(readPermission, writePermission), READ_WRITE_PERMISSION_REQUEST_CODE)
        }
    }

    private fun formatCell(cell: Cell): String {
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        // Retrieve the file from the URI
        // You can use the ContentResolver to open an InputStream and copy the file to a desired location

        // Example implementation using ContentResolver and InputStream:
        try {
            val resolver = contentResolver
            val inputStream = resolver.openInputStream(uri)

            // Create a temporary file to store the data
            val tempFile = File(cacheDir, "temp_file.csv")

            // Use streams to copy the data from the input stream to the temporary file
            val outputStream = FileOutputStream(tempFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            // Close the streams
            inputStream.close()
            outputStream.close()
            return tempFile
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle any errors that occur during file retrieval
        }
        return null
    }
    fun importCsvToDatabase(context: Context, csvFile: File) {

        GlobalScope.launch(Dispatchers.IO) {
            userDao.deleteAll()
            val yourEntities = parseCsvFile(csvFile)
            userDao.insertAll(yourEntities)
            onRestart()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@DashboardActivity,
                    "Successfully database restore",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    fun parseCsvFile(csvFile: File): List<Amount> {

        val amount = mutableListOf<Amount>()
        try {
            val reader = CSVReaderBuilder(FileReader(csvFile))
                .withSkipLines(1) // Skip the first line
                .build()

            var line: Array<String>?
            while (reader.readNext().also { line = it } != null) {
                if (line!!.size >= 2) {
                    var entity : Amount
                    if(line!![5].isEmpty()){
                         entity = Amount(
                            id = line!![0].toInt(),
                            ref_id = line!![4].toInt(),
                            name = line!![3].toString(),
                            category = line!![2].toString(),
                            price = line!![6].toDouble(),
                             date = stringDateToLong(line!![1],"dd/MM/yyyy"),
                            payment_mode = line!![7].toString(),
                            isIncome = false
                        )
                    } else {
                        entity = Amount(
                            id = line!![0].toInt(),
                            ref_id = line!![4].toInt(),
                            name = line!![3].toString(),
                            category =  line!![2].toString(),
                            price = line!![5].toDouble(),
                            date = stringDateToLong(line!![1],"dd/MM/yyyy"),
                            payment_mode =  line!![7].toString(),
                            isIncome = true
                        )
                    }

                    amount.add(entity)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return amount
    }
    fun stringDateToLong(dateString: String, format: String): Long {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return date?.time ?: -1L
    }
    fun storeTwoDecimalNumber(value: Double): Double {
        val decimalValue = BigDecimal(value)
            .setScale(2, RoundingMode.HALF_UP)

        return decimalValue.toDouble()
    }
    fun isCsvFile(fileName: String): Boolean {
        return fileName.endsWith(".csv", ignoreCase = true)
    }
    private fun getFileNameFromUri(uri: Uri): String? {
        val contentResolver = contentResolver
        var fileName: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = it.getString(displayNameIndex)
                }
            }
        }
        return fileName
    }
    }
