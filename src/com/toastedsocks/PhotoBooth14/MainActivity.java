package com.toastedsocks.PhotoBooth14;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import easycamera.DefaultEasyCamera;
import easycamera.EasyCamera;

import java.io.IOException;

public class MainActivity extends Activity implements SurfaceHolder.Callback{
    /**
     * Called when the activity is first created.
     */

    EasyCamera camera;
    EasyCamera.CameraActions actions;

    String mCurrentPhotoPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        camera = DefaultEasyCamera.open(1);
    }

    public void showPreview(View v) {

        this.setContentView(R.layout.camerapreview);
        SurfaceView cameraSurface = (SurfaceView)findViewById(R.id.cpPreview);
        SurfaceHolder holder = cameraSurface.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            actions = camera.startPreview(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void takePicture(View v) {
        EasyCamera.PictureCallback callback = new EasyCamera.PictureCallback() {
            public void onPictureTaken(byte[] data, EasyCamera.CameraActions actions) {
                // store picture
                Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);

                MediaStore.Images.Media.insertImage(getContentResolver(), picture, "name" , "description");
                SurfaceView cameraSurface = (SurfaceView)findViewById(R.id.cpPreview);
                try {
                    camera.startPreview(cameraSurface.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

        actions.takePicture(EasyCamera.Callbacks.create().withJpegCallback(callback));
        TextView myText = (TextView)findViewById(R.id.textView);

    }

    public void pictureDone() {

    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        try {
            actions = camera.startPreview(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

}
