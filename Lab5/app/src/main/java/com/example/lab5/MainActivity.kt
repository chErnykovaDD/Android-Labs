package com.example.lab5

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var proximitySensor: Sensor? = null

    private lateinit var lightValueText: TextView
    private lateinit var proximityValueText: TextView
    private lateinit var brightnessBar: ProgressBar
    private lateinit var screenStateText: TextView
    private lateinit var autoModeSwitch: Switch
    private lateinit var proximityModeSwitch: Switch

    private var isAutoMode = true
    private var isProximityEnabled = true
    private var currentLightValue = 0.0f
    private var isNear = false

    private val MAX_LUX = 10000.0f
    private val LIGHT_SENSOR_UPDATE_THRESHOLD = 10
    private val MIN_BRIGHTNESS = 10
    private val MAX_BRIGHTNESS = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lightValueText = findViewById(R.id.lightValueText)
        proximityValueText = findViewById(R.id.proximityValueText)
        brightnessBar = findViewById(R.id.brightnessBar)
        screenStateText = findViewById(R.id.screenStateText)
        autoModeSwitch = findViewById(R.id.autoModeSwitch)
        proximityModeSwitch = findViewById(R.id.proximityModeSwitch)

        val overlayView = findViewById<View>(R.id.brightnessOverlay)
        overlayView.isClickable = false
        overlayView.isFocusable = false

        overlayView.setOnClickListener {
            if (isNear && isProximityEnabled) {
                Toast.makeText(this, "Спроба розблокування через дотик", Toast.LENGTH_SHORT).show()
            }
        }

        autoModeSwitch.isChecked = isAutoMode
        proximityModeSwitch.isChecked = isProximityEnabled

        autoModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            isAutoMode = isChecked
            if (isChecked) {
                updateBrightness(currentLightValue)
            }
        }

        proximityModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            isProximityEnabled = isChecked
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (lightSensor == null) {
            Toast.makeText(this, "Датчик освітлення відсутній на пристрої", Toast.LENGTH_SHORT).show()
        }

        if (proximitySensor == null) {
            Toast.makeText(this, "Датчик наближення відсутній на пристрої", Toast.LENGTH_SHORT).show()
        }

        checkBrightnessPermission()
    }

    private fun checkBrightnessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(applicationContext)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "Будь ласка, дозвольте зміну системних налаштувань", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                val lightValue = event.values[0]
                currentLightValue = lightValue

                lightValueText.text = "Рівень освітлення: ${lightValue.toInt()} люкс"

                if (isAutoMode) {
                    updateBrightness(lightValue)
                }
            }

            Sensor.TYPE_PROXIMITY -> {
                val proximityValue = event.values[0]
                val maxRange = proximitySensor?.maximumRange ?: 5.0f

                isNear = proximityValue < maxRange / 2

                proximityValueText.text = "Датчик наближення: ${
                    if (isNear) "Об'єкт поблизу" else "Об'єкт далеко"
                }"

                if (isProximityEnabled) {
                    updateScreenState(isNear)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    private fun updateBrightness(lightValue: Float) {
        val normalizedLight = minOf(lightValue, MAX_LUX) / MAX_LUX
        val brightnessPercent = MIN_BRIGHTNESS + (MAX_BRIGHTNESS - MIN_BRIGHTNESS) * normalizedLight

        brightnessBar.progress = brightnessPercent.toInt()

        val maxDarkness = 0.8f
        val alpha = maxDarkness * (1.0f - (brightnessPercent / 100.0f))

        val colorOverlay = findViewById<View>(R.id.brightnessOverlay)
        colorOverlay.alpha = alpha

        Toast.makeText(this, "Яскравість встановлена: ${brightnessPercent.toInt()}%", Toast.LENGTH_SHORT).show()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(applicationContext)) {
                val brightnessValue = (brightnessPercent * 255 / 100).toInt()
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessValue)

                val layoutParams = window.attributes
                layoutParams.screenBrightness = brightnessPercent / 100
                window.attributes = layoutParams
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Помилка при зміні яскравості: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateScreenState(isNear: Boolean) {
        val overlayView = findViewById<View>(R.id.brightnessOverlay)

        if (isNear) {
            screenStateText.text = "Стан екрану: Заблокований"
            screenStateText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))

            // Для емуляції блокування екрану в тестовому режимі
            // Додаємо чорне накладення для симуляції блокування екрану
            overlayView.alpha = 0.98f

            overlayView.isClickable = true
            overlayView.isFocusable = true

            // ПРИМІТКА: В реальному додатку для повного блокування екрану потрібні
            // додаткові дозволи та використання DevicePolicyManager, який дозволяє
            // заблокувати також системні кнопки та жести. Це потребує, щоб додаток
            // мав права адміністратора пристрою:
            //
            // DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            // ComponentName adminComponent = new ComponentName(this, AdminReceiver.class);
            // if (dpm.isAdminActive(adminComponent)) {
            //     dpm.lockNow();
            // }

            Toast.makeText(this, "Екран заблоковано (наближення)", Toast.LENGTH_SHORT).show()

            brightnessBar.visibility = View.INVISIBLE
        } else {
            screenStateText.text = "Стан екрану: Активний"
            screenStateText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))

            brightnessBar.visibility = View.VISIBLE

            overlayView.isClickable = false
            overlayView.isFocusable = false

            if (isAutoMode) {
                updateBrightness(currentLightValue)
            } else {
                overlayView.alpha = 0.0f
            }
        }
    }
}