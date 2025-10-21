package com.example.airmonitorble

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.support.v18.scanner.*
import java.util.*
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bleManager: SimpleBleManager
    private val serviceUUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    private val ssidUUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    private val passUUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

    private lateinit var wifiListView: ListView
    private lateinit var scanBtn: Button
    private lateinit var connectBleBtn: Button
    private lateinit var statusText: TextView
    private lateinit var wifiManager: WifiManager
    private var wifiList: List<ScanResult> = emptyList()

    // Permissions required
    private val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
            )
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val allGranted = requiredPermissions.all { perm -> results[perm] == true }
            if (allGranted) {
                startWifiScan()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val goToDashboardBtn = findViewById<Button>(R.id.goToDashboardBtn)
//        goToDashboardBtn.setOnClickListener {
//            val intent = Intent(this, DashboardActivity::class.java)
//            startActivity(intent)
//
//        }
//        val goToHistoryBtn = findViewById<Button>(R.id.goToHistoryBtn)
//        goToHistoryBtn.setOnClickListener {
//            val intent = Intent(this, HistoryActivity::class.java)
//            startActivity(intent)
//        }


        wifiListView = findViewById(R.id.wifiListView)
        scanBtn = findViewById(R.id.scanWifiBtn)
        connectBleBtn = findViewById(R.id.connectBleBtn)
        statusText = findViewById(R.id.statusText)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        bleManager = SimpleBleManager(this, serviceUUID, ssidUUID, passUUID)

        connectBleBtn.setOnClickListener {
            if (permissionsGranted()) startScanAndConnect()
            else permissionLauncher.launch(requiredPermissions)
        }

        scanBtn.setOnClickListener {
            if (permissionsGranted()) startWifiScan()
            else permissionLauncher.launch(requiredPermissions)
        }

        wifiListView.setOnItemClickListener { _, _, position, _ ->
            val selectedSSID = wifiList[position].SSID
            showPasswordDialog(selectedSSID)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    true
                }

                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }

                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    private fun permissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startWifiScan() {
        if (!permissionsGranted()) {
            Toast.makeText(this, "Missing permissions for Wi-Fi scan", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (!wifiManager.isWifiEnabled) {
                Toast.makeText(this, "Enabling Wi-Fi...", Toast.LENGTH_SHORT).show()
                wifiManager.isWifiEnabled = true
            }

            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val success = wifiManager.startScan()
                if (success) {
                    wifiList = wifiManager.scanResults.distinctBy { it.SSID }
                        .filter { it.SSID.isNotEmpty() }
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        wifiList.map { "${it.SSID} (${it.level} dBm)" })
                    wifiListView.adapter = adapter
                    Toast.makeText(this, "Wi-Fi networks updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Wi-Fi scan failed", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "Missing permission to scan Wi-Fi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPasswordDialog(ssid: String) {
        val input = EditText(this)
        input.hint = "Enter password"

        AlertDialog.Builder(this).setTitle("Connect to $ssid").setView(input)
            .setPositiveButton("Send") { _: DialogInterface, _: Int ->
                val pass = input.text.toString()
                if (pass.isNotEmpty()) {
                    try {
                        bleManager.sendWiFi(ssid, pass)
                        statusText.text = "Sent Wi-Fi credentials for $ssid"
                    } catch (e: Exception) {
                        e.printStackTrace()
                        statusText.text = "Failed to send credentials"
                    }
                } else {
                    Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun startScanAndConnect() {
        statusText.text = "Scanning for AirMonitor BLE..."
        connectBleBtn.isEnabled = false
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    bleManager.scanAndConnect("AirMonitor BLE")
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
            statusText.text = if (success) "BLE Connected!" else "BLE Connect Failed"
            connectBleBtn.isEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()

        val prefs = getSharedPreferences("GasMonkeyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}

// ------------------- BLE Manager -------------------
class SimpleBleManager(
    private val context: Context,
    private val serviceUUID: UUID,
    private val ssidUUID: UUID,
    private val passUUID: UUID
) : BleManager(context) {

    private var ssidChar: android.bluetooth.BluetoothGattCharacteristic? = null
    private var passChar: android.bluetooth.BluetoothGattCharacteristic? = null

    override fun getGattCallback(): BleManagerGattCallback = object : BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt: android.bluetooth.BluetoothGatt): Boolean {
            return try {
                val service = gatt.getService(serviceUUID)
                ssidChar = service?.getCharacteristic(ssidUUID)
                passChar = service?.getCharacteristic(passUUID)
                service != null && ssidChar != null && passChar != null
            } catch (e: SecurityException) {
                e.printStackTrace()
                false
            }
        }

        override fun initialize() {}
        override fun onServicesInvalidated() {
            ssidChar = null
            passChar = null
        }
    }

    suspend fun scanAndConnect(deviceName: String): Boolean = withContext(Dispatchers.IO) {
        val scanner = BluetoothLeScannerCompat.getScanner()
        val filter = ScanFilter.Builder().setDeviceName(deviceName).build()
        val settings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        val job = CompletableDeferred<android.bluetooth.BluetoothDevice?>()

        val callback = object : ScanCallback() {
            override fun onScanResult(
                callbackType: Int, result: no.nordicsemi.android.support.v18.scanner.ScanResult
            ) {
                val foundName = result.device?.name
                if (foundName == deviceName && !job.isCompleted) job.complete(result.device)
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        scanner.stopScan(this)
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                if (!job.isCompleted) job.complete(null)
            }
        }

        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                scanner.startScan(listOf(filter), settings, callback)
            } catch (e: SecurityException) {
                e.printStackTrace()
                return@withContext false
            }
        } else return@withContext false

        val device = job.await()
        return@withContext if (device != null) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    connect(device).useAutoConnect(false).retry(3, 100).timeout(10000).enqueue()
                    true
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    false
                }
            } else false
        } else false
    }

    fun sendWiFi(ssid: String, pass: String) {
        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "Missing BLE connect permission", Toast.LENGTH_SHORT).show()
                return
            }

            ssidChar?.let {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    writeCharacteristic(it, ssid.toByteArray()).enqueue()
                }
            }

            GlobalScope.launch {
                delay(300)
                passChar?.let {
                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        writeCharacteristic(it, pass.toByteArray()).enqueue()
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to send Wi-Fi credentials.", Toast.LENGTH_LONG).show()
        }
    }
}
