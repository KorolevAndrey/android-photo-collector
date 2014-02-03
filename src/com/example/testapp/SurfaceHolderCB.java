package com.example.testapp;

import android.view.SurfaceHolder;

/**
 * Created by Yilin on 1/29/14.
 */
public class SurfaceHolderCB implements SurfaceHolder.Callback {
    boolean created = false;
    boolean destroyed = false;

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        created = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        created = false;
        destroyed = true;
    }
}
