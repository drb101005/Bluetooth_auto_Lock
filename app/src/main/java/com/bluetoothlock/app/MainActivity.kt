package com.bluetoothlock.app

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var adminButton: Button

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val DEVICE_ADMIN_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)

        statusText = findViewById(R.id.statusText)
        startButton = findViewById(R.id.startButton)
        adminButton = findViewById(R.id.adminButton)

        // Request permissions on startup
        requestAllPermissions()

        updateUI()

        adminButton.setOnClickListener {
            enableDeviceAdmin()
        }

        startButton.setOnClickListener {
            if (devicePolicyManager.isAdminActive(componentName)) {
                toggleService()
            } else {
                Toast.makeText(this, "Please enable Device Admin first!", Toast.LENGTH_LONG).show()
                enableDeviceAdmin()
            }
        }
    }

    private fun requestAllPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun enableDeviceAdmin() {
        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "This app needs Device Admin permission to lock your phone when Bluetooth device connects/disconnects.")
            }
            startActivityForResult(intent, DEVICE_ADMIN_REQUEST_CODE)
        } else {
            Toast.makeText(this, "Device Admin already enabled!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleService() {
        val serviceIntent = Intent(this, BluetoothLockService::class.java)

        if (isServiceRunning()) {
            stopService(serviceIntent)
            setServiceRunning(false)
            Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show()
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                setServiceRunning(true)
                Toast.makeText(this, "Service started! Check notification.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error starting service: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        // Update UI after a short delay to let service start
        startButton.postDelayed({ updateUI() }, 500)
    }

    private fun isServiceRunning(): Boolean {
        val prefs = getSharedPreferences("BluetoothLockPrefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("service_running", false)
    }

    private fun setServiceRunning(running: Boolean) {
        val prefs = getSharedPreferences("BluetoothLockPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("service_running", running).apply()
    }

    private fun updateUI() {
        val isRunning = isServiceRunning()
        val isAdmin = devicePolicyManager.isAdminActive(componentName)

        startButton.text = if (isRunning) "STOP SERVICE" else "START SERVICE"

        // Always enable start button if admin is active
        startButton.isEnabled = isAdmin

        val status = buildString {
            append("Device Admin: ${if (isAdmin) "✓ Enabled" else "✗ Disabled"}\n")
            append("Service: ${if (isRunning) "✓ Running" else "✗ Stopped"}\n\n")
            append("Target Device: Airdopes 148\n")
            append("MAC: 5D:1F:6F:FA:A5:CA\n\n")
            if (isRunning) {
                append("Check your notifications for confirmation.")
            } else if (!isAdmin) {
                append("Please enable Device Admin first!")
            }
        }

        statusText.text = status
        adminButton.isEnabled = !isAdmin
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions denied. App may not work fully.", Toast.LENGTH_LONG).show()
            }
            updateUI()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEVICE_ADMIN_REQUEST_CODE) {
            updateUI()
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Device Admin enabled! Now tap START SERVICE.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Device Admin not enabled. App won't work without it.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}