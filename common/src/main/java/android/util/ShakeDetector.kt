package android.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeDetector(context: Context) : SensorEventListener {
    private val sm: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var onShackedListener: (() -> Unit)? = null

    private var accelerometer = 0.0f // acceleration apart from gravity
    private var accelerometerCurrent = SensorManager.GRAVITY_EARTH // current acceleration including gravity
    private var before: Long = 0
    fun setOnShackedListener(onShackedListener: () -> Unit) {
        this.onShackedListener = onShackedListener
    }

    fun start() {
        sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        sm.unregisterListener(this, sensor)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val now = System.currentTimeMillis()
        val interval = now - before
        if (before == 0L || interval >= 0.5f * 1000) {    // 0.5초 이내에는 반응하지 않음
            before = now
            val x = event.values[0]
            val y = event.values[1]
            val lastAccelerometer = accelerometerCurrent
            accelerometerCurrent = Math.sqrt((x * x + y * y).toDouble()).toFloat()
            val delta = accelerometerCurrent - lastAccelerometer
            accelerometer = accelerometer * 0.9f + delta
            if (this.accelerometer > 10) { // 민감도
                onShackedListener?.invoke()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }
}
