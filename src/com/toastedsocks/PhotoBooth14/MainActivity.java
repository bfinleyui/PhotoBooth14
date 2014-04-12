package com.toastedsocks.PhotoBooth14;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;
import easycamera.DefaultEasyCamera;
import easycamera.EasyCamera;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements SurfaceHolder.Callback{
    /**
     * Called when the activity is first created.
     */

    MediaPlayer _shootMP;
    EasyCamera camera;
    EasyCamera.CameraActions actions;
    SurfaceView cameraSurface;
    String mCurrentPhotoPath;
    TextView myText;
    EasyCamera.PictureCallback callback;
    AlphaAnimation cameraFlash;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraFlash = new AlphaAnimation(1.0f, 0.0f);
        cameraFlash.setDuration(100);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.main);
        camera = DefaultEasyCamera.open(1);
        camera.enableShutterSound(true);
        this.setContentView(R.layout.camerapreview);

        SurfaceView cameraSurface = (SurfaceView)findViewById(R.id.cpPreview);
        SurfaceHolder holder = cameraSurface.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    public void showPreview(View v) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        List<Camera.Size> theSizes = camera.getParameters().getSupportedPreviewSizes();
        Camera.Parameters parameters = camera.getParameters();

        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();

        Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);

        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        camera.setParameters(parameters);
        try {
            actions = camera.startPreview(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w/h;

        if (sizes==null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }


        return optimalSize;
    }


    public void takePicture(View v) {
        v.setOnClickListener(null);
        callback = new EasyCamera.PictureCallback() {
            public void onPictureTaken(byte[] data, EasyCamera.CameraActions actions) {
                // store picture
                camera.stopPreview();
                Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);

                MediaStore.Images.Media.insertImage(getContentResolver(), picture, "name" , "description");
                cameraSurface = (SurfaceView)findViewById(R.id.cpPreview);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        try {
                            findViewById(R.id.cpPreview).setOnClickListener(new View.OnClickListener() {

                                                                                @Override
                                                                                public void onClick(View v)
                                                                                {
                                                                                    takePicture(v);
                                                                                }
                                                                            });
                            camera.startPreview(cameraSurface.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } , 3500);


            }
        };

        myText = (TextView)findViewById(R.id.textView);
        myText.setText("READY? SMILE!");
        new Handler().postDelayed(new Runnable() {
            public void run() {
                myText.setText("3..");
            }
        } , 1000);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                myText.setText("3..2...");
            }
        } , 2000);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                myText.setText("3..2..1..");
            }
        } , 3000);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                myText.setText("");


                FrameLayout flashView = (FrameLayout)findViewById(R.id.flashScreen);
                flashView.setAlpha(0.75f);
                flashView.animate().alpha(0).setDuration(100);
                shootSound();

                actions.takePicture(EasyCamera.Callbacks.create().withJpegCallback(callback));
            }
        } , 4000);



    }



    public void shootSound() {

        AudioManager meng = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int volume = meng.getStreamVolume( AudioManager.STREAM_NOTIFICATION);

        if (volume != 0)
        {
            if (_shootMP == null)
                _shootMP = MediaPlayer.create(MainActivity.this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            if (_shootMP != null)
                _shootMP.start();
        }
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
