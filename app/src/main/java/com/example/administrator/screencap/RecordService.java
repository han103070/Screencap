package com.example.administrator.screencap;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;


public class RecordService extends Service {
    private static MediaProjection mediaProjection;
    private static MediaRecorder mediaRecorder;
    private static VirtualDisplay virtualDisplay;

    private static boolean running = false;

//    private int width = 720;
//    private int height = 1080;
//    private int dpi = 320;

    DisplayMetrics dm = RecordApplication.getInstance().getResources().getDisplayMetrics();
    private int width = dm.widthPixels;
    private int height = dm.heightPixels;
    private int dpi = dm.densityDpi;

    static String ScreencapPath;
    static String videoName;


    @Override
    public IBinder onBind(Intent intent) {
        //    mediaRecorder = new MediaRecorder();
        return new RecordBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("service_thread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        //    running = false;
        //    mediaRecorder = new MediaRecorder();
    }

    @Override
    public void onDestroy() {
        Log.i("AAAAA-------","onDestory");
        super.onDestroy();

    }

    public void setMediaProject(MediaProjection project) {
        mediaProjection = project;
        //    mediaRecorder = new MediaRecorder();
    }

    public boolean isRunning() {
        return running;
    }

    public void setConfig(int width, int height, int dpi) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }

    public boolean startRecord() {
        if (mediaProjection == null || running) {
            return false;
        }

        initRecorder();
        createVirtualDisplay();
        //    mediaRecorder.setOnInfoListener((MediaRecorder.OnInfoListener) getApplicationContext());
        //    mediaRecorder.setOnErrorListener((MediaRecorder.OnErrorListener) getApplicationContext());
        mediaRecorder.start();
        running = true;
        return true;
    }

    public boolean stopRecord() {
        if (!running) {
            return false;
        }
        running = false;
        mediaRecorder.stop();
        //    mediaRecorder.reset();
        mediaRecorder.release();
        virtualDisplay.release();
        mediaProjection.stop();
        mediaRecorder = null;

        return true;
    }

    private void createVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
    }

    private void initRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        videoName = System.currentTimeMillis() + ".mp4";
        mediaRecorder.setOutputFile(getsaveDirectory() + videoName);
        mediaRecorder.setVideoSize(width, height);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mediaRecorder.setVideoFrameRate(30);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getScreencapPath(){
        ScreencapPath = File.separator +"xinlv" + File.separator + "Screencap" + videoName;
        return ScreencapPath;
    }

    public String getsaveDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"xinlv" + File.separator + "Screencap" + File.separator;//getAbsolutePath()
            File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }

            //      Toast.makeText(getBaseContext(), rootDir, Toast.LENGTH_SHORT).show();

            return rootDir;
        } else {
            return null;
        }
    }

    public class RecordBinder extends Binder {
        public RecordService getRecordService() {
            return RecordService.this;
        }
    }
}