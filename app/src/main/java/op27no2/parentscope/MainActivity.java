package op27no2.parentscope;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_CODE = 1;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 960;
    private static final int DISPLAY_HEIGHT = 1280;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private ToggleButton mToggleButton;
    private MediaRecorder mMediaRecorder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        System.out.println("start test");

        Button startBackService = (Button)findViewById(R.id.start);
        startBackService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start android service.
                Intent startServiceIntent = new Intent(MainActivity.this, MyService.class);
                startService(startServiceIntent);
                finish();
            }
        });

        Button stopBackService = (Button)findViewById(R.id.stop);
        stopBackService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop android service.
                Intent stopServiceIntent = new Intent(MainActivity.this, MyService.class);
                stopService(stopServiceIntent);
            }
        });


        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) this
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), this.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (this.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        System.out.println("granted " +granted);
        if(granted == false){
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            System.out.println("no permission");

            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO },
                    10);

        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            System.out.println("no permission");

            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    10);

        }else{
            System.out.println("setup");

            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1);

            for(int i=0; i<taskInfo.size();i++){
                String currentRunningActivityName = taskInfo.get(i).topActivity.getClassName();
                System.out.println("running: "+currentRunningActivityName);

            }



            initRecorder();
            prepareRecorder();
            mProjectionManager = (MediaProjectionManager) getSystemService
                    (Context.MEDIA_PROJECTION_SERVICE);

/*            mToggleButton = (ToggleButton) findViewById(R.id.toggle);
            mToggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onToggleScreenShare(v);
                }
            });*/

            mMediaProjectionCallback = new MediaProjectionCallback();

        }



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PERMISSION_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            mToggleButton.setChecked(false);
            return;
        }
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }

    public void startRecord(View view) {
            shareScreen();
       }


    public void stopRecord(View view) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            System.out.println("recording stopped");
            stopScreenSharing();
    }

    private void shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
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
            if (mToggleButton.isChecked()) {
                mToggleButton.setChecked(false);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                Log.v(TAG, "Recording Stopped");
            }
            mMediaProjection = null;
            stopScreenSharing();
            Log.i(TAG, "MediaProjection Stopped");
        }
    }

    private void prepareRecorder() {
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            finish();
        }
    }

/*
    public String getFilePath2() {
        File file = new File(this.getFilesDir(),"ParentScope");
        if(!file.exists()){
            file.mkdir();
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
*/

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


    private void checkBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            System.out.println("SYSTEM DOESNT SUPPORT BLUETOOTH)");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }



    }


}
