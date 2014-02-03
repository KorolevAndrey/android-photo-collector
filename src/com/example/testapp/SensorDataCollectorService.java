package com.example.testapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SensorDataCollectorService extends Service {
    private SensorManager mSensorManager;
    private XSensorEventListener mSensorListener;

    public SensorDataCollectorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "New Sensor Data Collector Service was Created", Toast.LENGTH_LONG).show();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new XSensorEventListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // For time consuming an long tasks you can launch a new thread here...
        Toast.makeText(this, "Sensor Data Collector Service Started", Toast.LENGTH_LONG).show();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Sensor Data Collector Service Destroyed", Toast.LENGTH_LONG).show();
        outputSensorData();
        mSensorManager.unregisterListener(mSensorListener);
    }

    private void outputSensorData() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/trackingData");
        File file1 = new File(dir, "acceReadings.txt");
        File file2 = new File(dir, "gyroReadings.txt");

        try {
            FileOutputStream f = new FileOutputStream(file1);
            List<Reading> readings = new ArrayList<Reading>(mSensorListener.acceReadings);
            readings = readings.subList(10, readings.size() - 10);
            for (Reading r : readings) {
                f.write((r.toString() + '\n').getBytes());
            }
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream f = new FileOutputStream(file2);
            List<Reading> readings = new ArrayList<Reading>(mSensorListener.gyroReadings);
            readings = readings.subList(10, readings.size() - 10);
            for (Reading r : readings) {
                f.write((r.toString() + '\n').getBytes());
            }
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Sensor data saved.", Toast.LENGTH_LONG).show();
    }
}
