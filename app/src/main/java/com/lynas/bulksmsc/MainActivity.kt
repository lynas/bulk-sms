package com.lynas.bulksmsc

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader


class MainActivity : AppCompatActivity() {
    lateinit var editText: EditText
    lateinit var sendButton: Button
    lateinit var readFileButton: Button
    lateinit var tvNumbers: TextView

    val numbersList = mutableListOf<String>()
    val numbersListMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.et_message)
        sendButton = findViewById(R.id.bt_send_sms)
        readFileButton = findViewById(R.id.bt_read_file)
        tvNumbers = findViewById(R.id.tv_numbers)

        sendButton.setOnClickListener {
            GlobalScope.launch {
                sendSms()
            }
        }
        readFileButton.setOnClickListener {
            readFile()
        }
    }

    fun readFile() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (permission == PackageManager.PERMISSION_GRANTED) {
            readFileWithPermission()
        } else {
            ActivityCompat.requestPermissions(
                this,
                listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray(),
                1
            )
        }
    }

    private fun readFileWithPermission() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("text/*")
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        numbersList.removeAll(numbersList)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val path = data?.data?.path
                val p = path?.substring(path.indexOf(":") + 1)
                val text = FileInputStream(p)
                BufferedReader(
                    InputStreamReader(
                        text
                    )
                ).use { br ->
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        val eachNumber = line?.trim() ?: ""
                        if (eachNumber.isNotEmpty()) {
                            numbersList.add(eachNumber)
                            numbersListMap[eachNumber] = "status:pending"

                        }
                    }
                }
                tvNumbers.setText(numbersListMap.toString())
            }
        }
    }

    suspend fun sendSms() {
        val permission =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            //send message
            sendMessageWithPermission()
        } else {
            ActivityCompat.requestPermissions(
                this,
                listOf(android.Manifest.permission.SEND_SMS).toTypedArray(),
                0
            )
        }
    }

    suspend fun sendMessageWithPermission() {
        val sms = editText.text.toString().trim()
        val smsManager = SmsManager.getDefault()
        withContext(Dispatchers.IO) {
            for (number in numbersList) {
                delay(5000)
                smsManager.sendTextMessage(number, null, sms, null, null)
            }
        }
    }
}

//class MyBroadcastReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        var deliveredPI =
//            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE)
//        switch (getResultCode()) {
//            case Activity.RESULT_OK:
//            Toast.makeText(getBaseContext(), "SMS delivered",
//                Toast.LENGTH_LONG).show();
//            finishActivity();
//            break;
//            case Activity.RESULT_CANCELED:
//            Toast.makeText(getBaseContext(), "SMS not delivered",
//                Toast.LENGTH_LONG).show();
//            finishActivity();
//            break;
//        if (intent == Intent.Action)
//
//    }
//
//}