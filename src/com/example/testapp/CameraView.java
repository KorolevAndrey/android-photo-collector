package com.example.testapp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback,
        Camera.PictureCallback
{

    private SurfaceHolder holder;
    private Camera camera;
    private Camera.Parameters parameters;
    private Activity act;
    private Handler handler = new Handler();
    private Context context;
    private SurfaceView surfaceView;
    private AudioManager audio;
    private int current;
    private boolean surfaceCreatedSuccessful = false;

    public CameraView(Context context) {
        super(context);

        surfaceView = this;
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        final int current = audio.getRingerMode();
        audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        this.context = context;
        holder = getHolder();// 生成Surface Holder
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 指定Push Buffer

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (camera == null || !surfaceCreatedSuccessful) {
                    handler.postDelayed(this, 1 * 1000);// 由于启动camera需要时间，在此让其等两秒再进行聚焦知道camera不为空
                } else {
                    camera.autoFocus(new AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success) {
                                camera.takePicture(new ShutterCallback() {// 如果聚焦成功则进行拍照
                                    @Override
                                    public void onShutter() {
                                    }
                                }, null, CameraView.this);
                            } else {
                            }
                        }
                    });
                }
            }
        }, 2 * 1000);
    }

    public CameraView(Context context, Activity act) {// 在此定义一个构造方法用于拍照过后把CameraActivity给finish掉
        this(context);
        this.act = act;
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        // TODO Auto-generated method stub

        camera = Camera.open();// 摄像头的初始化
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (holder != null) {
                    try {
                        camera.setPreviewDisplay(holder);

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    handler.postDelayed(this, 1 * 1000);
                }
            }
        }, 2 * 1000);
        surfaceCreatedSuccessful = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub

        parameters = camera.getParameters();
        camera.setParameters(parameters);// 设置参数
        camera.startPreview();// 开始预览
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        surfaceCreatedSuccessful = false;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {// 拍摄完成后保存照片

        try {

            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String time = format.format(date);

            //在SD卡上创建文件夹
            File file = new File(Environment.getExternalStorageDirectory()
                    + "/myCamera/pic");
            if (!file.exists()) {

                file.mkdirs();
            }

            String path = Environment.getExternalStorageDirectory()
                    + "/myCamera/pic/" + time + ".jpg";
            data2file(data, path);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
            holder.removeCallback(CameraView.this);
            audio.setRingerMode(current);
            act.finish();
            //uploadFile(path);

        } catch (Exception e) {

        }

    }

    private void data2file(byte[] w, String fileName) throws Exception {// 将二进制数据转换为文件的函数
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileName);
            out.write(w);
            out.close();
        } catch (Exception e) {
            if (out != null)
                out.close();
            throw e;
        }
    }
}
