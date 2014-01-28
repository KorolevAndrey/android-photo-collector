package com.example.testapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

public class XSensorEventListener implements SensorEventListener {
    final static int LOW = 1;
    final static int HIGH = 3;
    final static int MED = 2;
    int accuracy = 0;
    ArrayList<Reading> acceReadings = new ArrayList<Reading>();
    ArrayList<Reading> gyroReadings = new ArrayList<Reading>();

    @Override
    public void onSensorChanged(SensorEvent event) {
        Reading reading = new Reading(event, accuracy);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                acceReadings.add(reading);
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroReadings.add(reading);
                break;
            default:
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                this.accuracy = LOW;
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                this.accuracy = HIGH;
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                this.accuracy = MED;
                break;
            default:
        }
    }
}
