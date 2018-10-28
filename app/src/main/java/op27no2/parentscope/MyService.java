package op27no2.parentscope;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class MyService extends Service {
    Handler mHandler;
    Runnable mRunnable;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjection mMediaProjection;


    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 960;
    private static final int DISPLAY_HEIGHT = 1280;
    private MyService.MediaProjectionCallback mMediaProjectionCallback;
    private ToggleButton mToggleButton;
    private MyService myService;


    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private ServiceInterface serviceCallbacks;
    private Boolean isRecording = false;


    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        MyService getService() {
            // Return this instance of MyService so clients can call public methods
            return MyService.this;
        }
    }
    public void setCallbacks(ServiceInterface callbacks) {
        serviceCallbacks = callbacks;
    }



    @Override
    public void onCreate() {
        Toast.makeText(this, "Invoke background service onCreate method.", Toast.LENGTH_LONG).show();
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("service started");

        MyApplication.getScreenshotPermission();
        mMediaProjection = MyApplication.mMediaProjection;
        mMediaProjectionCallback = new MyService.MediaProjectionCallback();

        System.out.println("service media? "+ mMediaProjection);

        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        window.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;




        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {


                System.out.println("Foreground Task: "+printForegroundTask());


                mHandler.postDelayed(mRunnable, 2000); //100 ms you should do it 4000
            }
        };

        mHandler.postDelayed(mRunnable, 0);





        return super.onStartCommand(intent, flags, startId);


    }

    private String printForegroundTask() {
        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("WrongConstant") UsageStatsManager usm = (UsageStatsManager)this.getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        Log.e("adapter", "Current App in foreground is: " + currentApp);

        if(currentApp.equals("com.facebook.katana") && isRecording==false){
            isRecording = true;
            System.out.println("Launching Record Activity");
            mMediaProjection = MyApplication.mMediaProjection;
            mMediaProjectionCallback = new MyService.MediaProjectionCallback();

            System.out.println("service media2? "+ mMediaProjection);


            initRecorder();
            prepareRecorder();
            startRecord();

          /*  Intent dialogIntent = new Intent(this, RecordActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(dialogIntent);*/

            System.out.println("check callbacks");

    /*        if (serviceCallbacks != null) {
                serviceCallbacks.start();
                System.out.println("START RECORD");

            }
*/
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRecord();
                    System.out.println("STAHP RECORD");

                }
            }, 6000);


        }


        return currentApp;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("service destroyed");

        if(mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }


    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android


        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);

    }



    public void startRecord() {

        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        System.out.println("service started");

    }


    public void stopRecord() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        System.out.println("recording stopped");
        stopScreenSharing();
    }



    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null /*Handler*/);
    }




    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    private void prepareRecorder() {
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }


    public String getFilePath() {
        final String directory = Environment.getExternalStorageDirectory() + File.separator + "ParentScope";
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "Failed to get External Storage", Toast.LENGTH_SHORT).show();
            return null;
        }
        final File folder = new File(directory);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        String filePath;
        if (success) {
            String videoName = ("capture_" + getCurSysDate() + ".mp4");
            filePath = directory + File.separator + videoName;
        } else {
            Toast.makeText(this, "Failed to create Recordings directory", Toast.LENGTH_SHORT).show();
            return null;
        }
        return filePath;
    }

    public String getCurSysDate() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    }

    private void initRecorder() {


        if (mMediaRecorder == null) {
            System.out.println("recorder init");

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(3000000);
            mMediaRecorder.setCaptureRate(2);
            mMediaRecorder.setVideoFrameRate(2);
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setOutputFile(getFilePath());
        }
    }








}