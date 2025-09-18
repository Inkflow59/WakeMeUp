# 📍 WakeMeUp - Location-Based Alarm

<div align="center">

![Android](https://img.shields.io/badge/Android-21%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![OpenStreetMap](https://img.shields.io/badge/OpenStreetMap-Free-7EBC6F?style=for-the-badge&logo=openstreetmap&logoColor=white)
![GPS](https://img.shields.io/badge/GPS-Location-FF6B35?style=for-the-badge&logo=location&logoColor=white)

*Never miss your stop again! Wake up automatically when you approach your destination.*

</div>

## 🎯 About

**WakeMeUp** is an innovative Android application that allows you to create alarms based on your geographical location rather than time. Perfect for:

- 🚊 **Public Transportation**: Sleep peacefully, the app wakes you up at your stop
- 🚗 **Car Travel**: Ideal for passengers during long journeys
- ✈️ **Travel**: Never miss your connections or destinations again
- 🏃 **Sports**: Wake-up calls for your checkpoints during long runs

## ✨ Features

### 🎛️ Alarm Management
- **Intuitive Creation**: Simple interface with interactive OpenStreetMap
- **Complete Customization**: Name, position, activation radius (10m - 1km)
- **Flexible Options**: Sound, vibration, enable/disable
- **Address Search**: Easily find your destinations

### 📍 Geolocation Technology
- **Continuous Monitoring**: Optimized background service
- **GPS Precision**: Uses fine location services
- **Power Efficient**: Intelligent battery management
- **Offline Operation**: No internet needed once maps are downloaded

### 🔔 Alert System
- **Push Notifications**: Immediate and visible alerts
- **Alarm Sound**: Loud and customizable ringtone
- **Vibration**: Tactile wake-up even in silent mode
- **Auto-Deactivation**: Alarm deactivates after triggering

### 📱 Android Widget
- **Home Screen Widget**: Quick view of active alarms directly on your home screen
- **Real-time Updates**: Widget automatically refreshes when alarms change
- **One-tap Access**: Direct launch to the main app from the widget
- **Compact Display**: Shows essential information without cluttering your screen

### 🔄 System Integration
- **Auto-restart**: Automatically restarts monitoring after device reboot
- **Background Persistence**: Maintains alarm monitoring even when app is closed
- **System Broadcasts**: Efficient communication between app components
- **Boot Receiver**: Seamlessly resumes active alarms after system restart

## 🚀 Installation

### Prerequisites
- **Android 5.0** (API 21) or higher
- **Internet connection** (to download maps)
- **GPS** enabled on device

### Installation Steps

1. **Clone the project**
   ```bash
   git clone https://github.com/your-username/wakemeup.git
   cd wakemeup
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - File → Open → Select project folder

3. **Build and install**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

**🎉 No additional configuration required!** The app uses OpenStreetMap which doesn't require an API key.

## 🔧 Configuration

### Required Permissions

The app automatically requests the following permissions:

```xml
<!-- Geolocation -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Notifications and wake-up -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.VIBRATE" />

<!-- Auto-restart after reboot -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- OpenStreetMap -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### Battery Optimization

For optimal operation, disable battery optimization for WakeMeUp:

1. **Settings** → **Battery** → **Battery Optimization**
2. Search for **WakeMeUp**
3. Select **"Don't optimize"**

### Widget Setup

To add the WakeMeUp widget to your home screen:

1. **Long press** on your home screen
2. Select **"Widgets"**
3. Find **"WakeMeUp"** in the list
4. **Drag and drop** to your desired location
5. The widget will automatically show your active alarms

## 📖 User Guide

### 1. Create your first alarm

1. **Open the app** and tap the **[+]** button
2. **Name your alarm** (e.g., "Châtelet Stop")
3. **Set the position**:
   - 📍 Use your current location
   - 🔍 Search for an address
   - 🗺️ Click directly on the map
4. **Configure the activation radius** (recommended: 100-200m)
5. **Enable sound and/or vibration**
6. **Save**

### 2. Use the alarm

1. **Activate the alarm** in the main list
2. **Start your journey** - the app monitors your position in the background
3. **Get alerted** automatically when you approach the destination
4. **Wake up** thanks to sound and vibration

### 3. Advanced Management

- **✏️ Edit**: Click "Edit" to change settings
- **🔄 Enable/Disable**: Use the switch to temporarily enable/disable
- **🗑️ Delete**: Click "Delete" to permanently remove
- **📱 Widget**: Monitor active alarms directly from your home screen

## 🏗️ Architecture

### Project Structure

```
app/
├── 📱 MainActivity.kt          # Main screen
├── ✏️ AlarmEditorActivity.kt   # Alarm creation/modification
├── 📍 LocationService.kt       # Geolocation service
├── 💾 AlarmRepository.kt       # Data management
├── 📋 AlarmAdapter.kt          # List display
├── 🏠 LocationAlarm.kt         # Data model
├── 🎛️ MainViewModel.kt        # Business logic
├── 📱 WakeMeUpWidget.kt        # Home screen widget
├── 🔄 BootReceiver.kt          # Auto-restart after reboot
└── 🔐 PermissionManager.kt     # Permission management
```

### Technologies Used

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Geolocation**: Google Play Services Location
- **Maps**: OpenStreetMap with osmdroid
- **Storage**: SharedPreferences
- **Interface**: Material Design Components
- **Services**: Foreground Service for background operation
- **Widgets**: Android App Widget framework
- **System Integration**: Broadcast Receivers

### Key Components

| Component | Role | Technology |
|-----------|------|-------------|
| `LocationService` | Background GPS monitoring | FusedLocationProviderClient |
| `AlarmRepository` | Data persistence | SharedPreferences |
| `MainActivity` | Main interface | RecyclerView + LiveData |
| `AlarmEditorActivity` | Alarm configuration | OpenStreetMap |
| `WakeMeUpWidget` | Home screen widget | AppWidgetProvider |
| `BootReceiver` | Auto-restart on boot | BroadcastReceiver |

## 🔒 Privacy

- **Local Data**: All data is stored locally on your device
- **No Server**: No data is sent to external servers
- **GPS Only**: Only GPS location is used, no other personal data
- **Transparent**: Open source code, auditable

## 🐛 Troubleshooting

### Alarm doesn't trigger

- ✅ Check that the alarm is **enabled** (green switch)
- ✅ Confirm that **location permissions** are granted
- ✅ Disable **battery optimization** for the app
- ✅ Verify that **GPS is enabled** on the device
- ✅ Ensure you're within the **defined zone** (configured radius)

### App closes in background

- ✅ Disable **battery optimization** for WakeMeUp
- ✅ Add the app to **protected applications** (manufacturer dependent)
- ✅ Check in **Settings** → **Apps** → **Special Permissions**

### Widget not updating

- ✅ Ensure the widget has been **added to home screen**
- ✅ Check that the app has **notification permissions**
- ✅ Try **removing and re-adding** the widget
- ✅ Verify that **background app refresh** is enabled

### Alarms don't restart after reboot

- ✅ Grant **"Auto-start"** permission (manufacturer dependent)
- ✅ Ensure **RECEIVE_BOOT_COMPLETED** permission is granted
- ✅ Check that active alarms exist before restart
- ✅ Disable **deep sleep mode** for the app

### Inaccurate GPS

- ✅ Enable **high-precision location**
- ✅ Be in an **open environment** (avoid tunnels/buildings)
- ✅ Increase the **activation radius** if necessary

## 🤝 Contributing

Contributions are welcome! To contribute:

1. **Fork** the project
2. Create a **feature branch** (`git checkout -b feature/new-feature`)
3. **Commit** your changes (`git commit -m 'Add new feature'`)
4. **Push** to the branch (`git push origin feature/new-feature`)
5. Open a **Pull Request**

### Improvement Ideas

- 🌙 **Dark mode** for the interface
- 📊 **Usage statistics** for alarms
- 🎵 **Custom sounds** for alarms
- 🌐 **Multi-language support**
- ⏰ **Combined alarms** (time + location)
- 📈 **Advanced widget features** (progress bars, distance indicators)
- 🔔 **Smart notifications** based on user patterns
- 🗺️ **Offline maps** for better performance

## 📄 License

This project is under MIT License. See the [LICENSE](LICENSE) file for more details.

## 👨‍💻 Developer

Developed with ❤️ to make travel more peaceful.

---

<div align="center">

**⭐ Don't hesitate to star this project if it helps you! ⭐**

*WakeMeUp - Because your destination matters more than time*

</div>
