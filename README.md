# Bluetooth_auto_Lock

An Android app that automatically locks your phone when a specific Bluetooth device connects or disconnects. Perfect for adding an extra layer of security with your everyday Bluetooth devices!

## 🎯 Features
- **Automatic Phone Locking**: Locks your phone instantly when your Bluetooth device connects or disconnects
- **Background Service**: Runs persistently in the background with a foreground notification
- **Device-Specific**: Targets a specific Bluetooth device by name and MAC address
- **Low Battery Impact**: Efficient Bluetooth monitoring with minimal battery drain
- **Android 15 Compatible**: Fully tested and working on the latest Android version

## 📱 Use Cases
- Lock phone when you put on/remove your wireless earbuds
- Security trigger with any Bluetooth device
- Automatic locking when disconnecting from car Bluetooth
- Privacy protection when connecting to untrusted Bluetooth devices

## 🔧 How It Works
The app uses Android's Bluetooth broadcast receivers to monitor connection state changes. When your specified device (by name or MAC address) connects or disconnects, it triggers the Device Admin API to lock the screen immediately.

### Current Configuration:
- **Target Device**: Airdopes 148
- **MAC Address**: 5D:1F:6F:FA:A5:CA
- **Action on Connect**: Lock Phone
- **Action on Disconnect**: Lock Phone

## 📋 Requirements
- Android 8.0 (API 26) or higher
- Android 15 fully supported
- Bluetooth-enabled device
- Device Administrator permission

## 🚀 Installation
### Option 1: Install from Release (Easiest)
1. Download the latest APK from Releases
2. Enable "Install from Unknown Sources" for your browser/file manager
3. Install the APK
4. Follow the in-app setup instructions

### Option 2: Build from Source
1. Clone this repository
    ```bash
    git clone https://github.com/yourusername/bluetooth-auto-lock.git
    ```
2. Open the project in Android Studio
3. Update the target device details in `BluetoothLockService.kt`:
    ```kotlin
    private const val TARGET_DEVICE_NAME = "Your Device Name"
    private const val TARGET_DEVICE_MAC = "XX:XX:XX:XX:XX:XX"
    ```
4. Build and install:
    - Build → Build Bundle(s) / APK(s) → Build APK(s)
    - Install the generated APK on your device

## ⚙️ Setup
1. Launch the app
2. **Grant Permissions**: Allow all requested permissions (Bluetooth, Location, Notifications)
3. **Enable Device Admin**: Tap "ENABLE DEVICE ADMIN" and approve the permission
4. **Start Service**: Tap "START SERVICE"
5. **Verify**: Check for the "Bluetooth Auto Lock Active" notification
6. **Test**: Connect/disconnect your Bluetooth device to verify it works

### Important: Battery Optimization
To prevent Android from killing the service:

1. Go to Settings → Apps → Bluetooth Auto Lock
2. Tap Battery → Select "Unrestricted"

## 📂 Project Structure
app/
├── src/main/java/com/bluetoothlock/app/
│ ├── MainActivity.kt # Main UI and permission handling
│ ├── BluetoothLockService.kt # Background service for monitoring
│ └── DeviceAdminReceiver.kt # Device admin policy receiver
├── res/
│ ├── layout/
│ │ └── activity_main.xml # App UI layout
│ └── xml/
│ └── device_admin.xml # Device admin policy definition
└── AndroidManifest.xml # App configuration and permissions

perl
Copy code

## 🔐 Permissions Used

| Permission                       | Purpose                                      |
|-----------------------------------|----------------------------------------------|
| `BLUETOOTH_CONNECT`               | Connect to Bluetooth devices                 |
| `BLUETOOTH_SCAN`                  | Scan for Bluetooth devices                   |
| `ACCESS_FINE_LOCATION`            | Required by Android for Bluetooth scanning   |
| `FOREGROUND_SERVICE`              | Run persistent background service            |
| `FOREGROUND_SERVICE_CONNECTED_DEVICE` | Declare foreground service type             |
| `POST_NOTIFICATIONS`              | Show persistent notification                 |
| `Device Admin`                    | Lock the device screen                       |

## 🛠️ Customization
### Change Target Device
Edit `BluetoothLockService.kt`:
```kotlin
companion object {
    private const val TARGET_DEVICE_NAME = "Your Device Name"
    private const val TARGET_DEVICE_MAC = "XX:XX:XX:XX:XX:XX"
}
Add Multiple Devices
Modify the isTargetDevice() function to check multiple devices:

kotlin
Copy code
private fun isTargetDevice(device: BluetoothDevice): Boolean {
    val targetDevices = listOf(
        "Device1" to "MAC1",
        "Device2" to "MAC2"
    )
    return targetDevices.any { (name, mac) ->
        device.name == name || device.address == mac
    }
}
Change Lock Behavior
In BluetoothLockService.kt, modify the broadcast receiver to lock only on connect or only on disconnect:

kotlin
Copy code
// Lock only on connect
BluetoothDevice.ACTION_ACL_CONNECTED -> {
    // ... lock phone
}

// Remove the ACTION_ACL_DISCONNECTED case to disable lock on disconnect
🐛 Troubleshooting
Service not starting:

Ensure all permissions are granted

Check that Device Admin is enabled

Disable battery optimization for the app

Phone not locking:

Verify your device name and MAC address are correct

Check that the "Bluetooth Auto Lock Active" notification is visible

Ensure Device Admin permission is active

Service stops after phone restart:

Open the app and tap START SERVICE again
(This is an Android security requirement)

🤝 Contributing
Contributions are welcome! Feel free to:

Report bugs

Suggest new features

Submit pull requests

📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

🙏 Acknowledgments
Built with love and persistence through extensive troubleshooting

Thanks to the Android developer community for documentation and resources

📬 Contact
For questions or suggestions, please open an issue on GitHub.

⭐ If you find this useful, please star the repository!
