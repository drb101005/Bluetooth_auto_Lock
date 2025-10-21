package com.bluetoothlock.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BluetoothLockService : Service() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private var bluetoothReceiver: BroadcastReceiver? = null
    
    companion object {
        private const val CHANNEL_ID = "BluetoothLockChannel"
        private const val NOTIFICATION_ID = 1
        private const val TARGET_DEVICE_NAME = "Airdopes 148"
        private const val TARGET_DEVICE_MAC = "5D:1F:6F:FA:A5:CA"
    }

    override fun onCreate() {
        super.onCreate()
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        registerBluetoothReceiver()
        setServiceRunning(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun registerBluetoothReceiver() {
        bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            if (isTargetDevice(it)) {
                                lockPhone("Connected to ${it.name}")
                            }
                        }
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            if (isTargetDevice(it)) {
                                lockPhone("Disconnected from ${it.name}")
                            }
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        
        registerReceiver(bluetoothReceiver, filter)
    }

    private fun isTargetDevice(device: BluetoothDevice): Boolean {
        val deviceName = try {
            device.name
        } catch (e: SecurityException) {
            null
        }
        
        val deviceAddress = device.address
        
        return deviceName == TARGET_DEVICE_NAME || deviceAddress == TARGET_DEVICE_MAC
    }

    private fun lockPhone(reason: String) {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bluetooth Lock Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the Bluetooth lock service running"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bluetooth Auto Lock Active")
            .setContentText("Monitoring Airdopes 148")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun setServiceRunning(running: Boolean) {
        val prefs = getSharedPreferences("BluetoothLockPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("service_running", running).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothReceiver?.let {
            unregisterReceiver(it)
        }
        setServiceRunning(false)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}