package com.example.testapp;

import android.hardware.SensorEvent;

class Reading {
    float x; float y; float z; String timestamp; int accuracy;
    Reading(SensorEvent e, int accuracy) {
        this.x = -e.values[0];
        this.y = -e.values[1];
        this.z = -e.values[2];
        //SimpleDateFormat sdf = new SimpleDateFormat("SSSSSSS");
        //this.timestamp = sdf.format(new Date(e.timestamp / 1000000));
        this.timestamp = String.valueOf(System.nanoTime());
        //Log.d(MainActivity.DEBUG_TAG, String.valueOf(e.timestamp));
        //this.timestamp = String.valueOf(new Date().getTime() + (e.timestamp - System.nanoTime()) / 1000000L);
        this.accuracy = accuracy;
    }
    public String toString() {
        return String.format("%s %f %f %f %s", timestamp, x, y, z, accuracy);
    }
}
