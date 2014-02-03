package com.example.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
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

    private SurfaceHolderCB surfaceHolderCB = new SurfaceHolderCB();

    private Camera camera;
    private SensorManager mSensorManager;
    private XSensorEventListener mSensorListener;
    private Handler mHandler = new Handler();
    private SurfaceView mSurfaceView01;
    private SurfaceHolder mSurfaceHolder01;
    private boolean isCameraReady = false;
    private Button takePicButton;
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
            }
            mHandler.postDelayed(timerTask, 300);
        }
    };
    private Runnable cameraTask = new Runnable() {
        @Override
        public void run() {
            if (surfaceHolderCB.created) {
                initCamera();
            }


            if (started) {
                takePicture();
                //new TakePictureTask();
            }
            mHandler.postDelayed(cameraTask, 500);
        }
    };

    private boolean started = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        mSurfaceView01 = (SurfaceView) findViewById(R.id.mSurfaceView1);
        mSurfaceHolder01 = mSurfaceView01.getHolder();
        mSurfaceHolder01.addCallback(surfaceHolderCB);
        mSurfaceHolder01.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        takePicButton = (Button) findViewById(R.id.digPhotoButton);

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

        takePicButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                    takePicture();
                }
        });

        final Button saveBtn = (Button)findViewById(R.id.button2);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outputSensorData();
            }
        });
    }

    private void start() {
        mSensorListener = new XSensorEventListener();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        startService(new Intent(this, SensorDataCollectorService.class));
        mHandler.post(timerTask);
        mHandler.post(cameraTask);
    }
    private void stop() {
        mSensorManager.unregisterListener(mSensorListener);
        mHandler.removeCallbacks(timerTask);
        mHandler.removeCallbacks(cameraTask);
        stopService(new Intent(this, SensorDataCollectorService.class));
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


    private void takePicture() {
        if (camera != null && isCameraReady) {
            //camera.autoFocus(new Camera.AutoFocusCallback() {
            //    @Override
            //    public void onAutoFocus(boolean success, Camera camera) {
            //        if (success) {
            //            camera.takePicture(null, null, jpegCallback);
            //        } else {
            //            Log.d(DEBUG_TAG, "Autofocus failed");
            //        }
            //    }
            //});
            camera.takePicture(null, null, jpegCallback);
            isCameraReady = false;
        }
    }

    private void initCamera() {
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            if (camera == null) {
                int cameraId = findFrontFacingCamera();
                if (cameraId < 0) {
                    Toast.makeText(this, "No back facing camera found.",
                            Toast.LENGTH_LONG).show();
                } else {
                    camera = Camera.open(cameraId);
                    Camera.Parameters params = camera.getParameters();
                    params.setPictureSize(640, 480);
                    params.setPreviewSize(640, 480);
                    camera.setParameters(params);
                    try {
                        camera.setPreviewDisplay(mSurfaceHolder01);
                        camera.setDisplayOrientation(90);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();
                    isCameraReady = true;
                }
        }   }
    }

    private void resetCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
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
                camera.takePicture(null, null, jpegCallback);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Log.d(DEBUG_TAG, "taking picture failed!");
        }
    }


    private class TakePictureTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void result) {
            // This returns the preview back to the live camera feed
            //camera.startPreview();
        }

        @Override
        protected Void doInBackground(Void... params) {
            camera.takePicture(null, null, jpegCallback);
            return null;
        }

    }


    class SavePhotoTask extends AsyncTask<byte[], String, String> {
        @Override
        protected String doInBackground(byte[]... data) {
            File pictureFileDir = getDir();

            if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

                Log.d(MainActivity.DEBUG_TAG, "Can't create directory to save image.");
                //Toast.makeText(getApplicationContext(), "Can't create directory to save image.",
                //        Toast.LENGTH_LONG).show();
                return null;

            }

            //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
            //String date = dateFormat.format(new Date());
            String photoFile = System.nanoTime() + ".jpg";

            String filename = pictureFileDir.getPath() + File.separator + photoFile;

            File pictureFile = new File(filename);

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data[0]);
                fos.close();
                //Toast.makeText(getApplicationContext(), "New Image saved:" + photoFile,
                //        Toast.LENGTH_LONG).show();
                Log.d(MainActivity.DEBUG_TAG, "Picture saved!");
            } catch (Exception error) {
                Log.d(MainActivity.DEBUG_TAG, "File" + filename + "not saved: "
                        + error.getMessage());
                //Toast.makeText(getApplicationContext(), "Image could not be saved.",
                //        Toast.LENGTH_LONG).show();
            }

            return(null);
        }
    }

    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera _camera) {
            new SavePhotoTask().execute(data);
            resetCamera();
            initCamera();
        }
    };
    private File getDir() {
        File sdDir = Environment.getExternalStorageDirectory();
        return new File(sdDir.getAbsolutePath(), "/trackingData");
    }
}
