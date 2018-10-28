package op27no2.parentscope;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;

public class MyApplication extends android.app.Application {

    private static Intent screenshotPermission = null;
    private static MyApplication instance;
    public static MediaProjection mMediaProjection;
    public static Context context;


    public void onCreate() {
        super.onCreate();
        System.out.println("APPLICATION ONCREATE");
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
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
        getAppContext() .startActivity(intent);
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
