package op27no2.parentscope2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.Set;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends android.app.Application {

    private static Intent screenshotPermission = null;
    private static MyApplication instance;
    public static MediaProjection mMediaProjection;
    public static Context context;

    //btxfr code
    private static String TAG = "BTPHOTO/MainApplication";
    protected static BluetoothAdapter adapter;
    protected static Set<BluetoothDevice> pairedDevices;
    protected static Handler clientHandler;
    protected static Handler serverHandler;
    protected static zClientThread clientThread;
    protected static zServerThread serverThread;
    protected static zProgressData progressData = new zProgressData();

    protected static final String TEMP_IMAGE_FILE_NAME = "btimage.jpg";
    protected static final int PICTURE_RESULT_CODE = 1234;
    protected static final int IMAGE_QUALITY = 100;

    public void onCreate() {

        super.onCreate();
        System.out.println("APPLICATION ONCREATE");
        MyApplication.context = getApplicationContext();
        instance = this;

        Fabric.with(this, new Crashlytics());


        //btxfr
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (adapter.isEnabled()) {
                pairedDevices = adapter.getBondedDevices();
            } else {
                Log.e(TAG, "Bluetooth is not enabled");
            }
        } else {
            Log.e(TAG, "Bluetooth is not supported on this device");
        }
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static Handler getClientHandlert() {
        return MyApplication.clientHandler;
    }


    protected static void getScreenshotPermission() {
        try {
           /* if (hasScreenshotPermission()) {
                if(null != mMediaProjection) {
                    mMediaProjection.stop();
                    mMediaProjection = null;
                }

                MediaProjectionManager mProjectionManager = (MediaProjectionManager) getContext().getSystemService
                        (Context.MEDIA_PROJECTION_SERVICE);

                mMediaProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) screenshotPermission.clone());
            } else {*/

        openScreenshotPermissionRequester();

    } catch (final RuntimeException ignored) {
            openScreenshotPermissionRequester();
        }
    }

    protected static void openScreenshotPermissionRequester(){
        System.out.println(" SS REQUEST" );

        final Intent intent = new Intent(getAppContext() , AcquireScreenshotPermissionIntent.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getAppContext().startActivity(intent);
    }



    protected static void setScreenshotPermission(final Intent permissionIntent) {
        screenshotPermission = permissionIntent;
        MediaProjectionManager mProjectionManager = (MediaProjectionManager) getAppContext() .getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);

        mMediaProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) screenshotPermission.clone());
    }





    private static boolean hasScreenshotPermission(){



        return true;
    }



}
