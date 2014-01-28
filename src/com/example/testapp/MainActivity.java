package com.example.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    final static String DEBUG_TAG = "Main";
    private Camera camera;
    private SensorManager mSensorManager;
    private XSensorEventListener mSensorListener;
    private Handler mHandler = new Handler();
    private Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            if (started) {
                if (!mSensorListener.acceReadings.isEmpty()) {
                    TextView textView = (TextView)findViewById(R.id.tv1);
                    textView.setText(("Acc: " + mSensorListener.acceReadings.get(mSensorListener.acceReadings.size() - 1)));
                }
                if (!mSensorListener.gyroReadings.isEmpty()) {
                    TextView textView = (TextView)findViewById(R.id.tv2);
                    textView.setText(("Gyro: " + mSensorListener.gyroReadings.get(mSensorListener.gyroReadings.size() - 1)));
                }
                //takePictureNoPreview(getApplicationContext());
            }
            mHandler.postDelayed(timerTask, 300);
        }
    };

    private CameraView cameraView;


    private boolean started = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        final Button startBtn = (Button)findViewById(R.id.button1);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                started = !started;
                if (started) {
                    start();
                    startBtn.setText("Stop");
                } else {
                    stop();
                    startBtn.setText("Start");
                }
            }
        });

        final Button saveBtn = (Button)findViewById(R.id.button2);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outputSensorData();
            }
        });

        //initCamera();

        cameraView = new CameraView(this);
        cameraView.setId(1);
        cameraView = new CameraView(this, this);
        setContentView(R.layout.camera_layout);
        RelativeLayout relative = (RelativeLayout) this.findViewById(R.id.ly);
        RelativeLayout.LayoutParams Layout = new RelativeLayout.LayoutParams(3, 3);// 设置surfaceview使其满足需求无法观看预览
        Layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
        Layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);

        relative.addView(cameraView, Layout);


    }

    private void start() {
        mSensorListener = new XSensorEventListener();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        mHandler.post(timerTask);
    }
    private void stop() {
        mSensorManager.unregisterListener(mSensorListener);
        mHandler.removeCallbacks(timerTask);
    }


    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    @Override
    protected void onPause() {
        stop();
        super.onPause();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//private void collectData() {
	//    Timer timer = new Timer();
	//    timer.schedule(new TimerTask() {
	//		@Override
	//		public void run() {
     //           if (started) {
     //               if (camera != null) {
     //                   camera.takePicture(null, null,
     //                           new PhotoHandler(getApplicationContext()));
     //               }
     //           }
	//		}
	//	},0,1000);
	//}


    private void initCamera() {
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No back facing camera found.",
                        Toast.LENGTH_LONG).show();
            } else {
                camera = Camera.open(cameraId);
                //camera.enableShutterSound(true);
            }
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d(DEBUG_TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private void outputSensorData() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/trackingData");
        dir.mkdirs();
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
    }

    //Here is the example for dummy surface view.

    public void takePictureNoPreview(Context context){
        // open back facing camera by default
        //Camera myCamera = Camera.open();
        if(camera != null){
            try {
                //set camera parameters if you want to
                //...

                // here, the unused surface view and holder
                SurfaceView dummy = new SurfaceView(context);
                camera.setPreviewDisplay(dummy.getHolder());
                camera.startPreview();

                Log.d(DEBUG_TAG, "starting taking picture!");
                System.gc();
                camera.takePicture(null, null, new PhotoHandler(context));

            } catch (IOException e) {
                e.printStackTrace();
            }
            //} catch (RuntimeException e) {
            //    e.printStackTrace();
            //}

        } else {
            Log.d(DEBUG_TAG, "taking picture failed!");
        }
    }
}
