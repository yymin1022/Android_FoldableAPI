package com.yong.foldabletest.fold

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Hinge angle in degrees (0 = closed, 180 = flat) from [Sensor.TYPE_HINGE_ANGLE].
 * - Independent of WindowManager, so it reports the physical angle even when no FoldingFeature is given
 * - Returns null if the device does not expose the sensor
 * - The sensor is registered only while the flow is collected
 */
fun Context.hingeAngleFlow(): Flow<Float>? {
    val sensorManager = getSystemService(SensorManager::class.java)
    val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE) ?: return null

    return callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(event.values[0])
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
