package op27no2.parentscope;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import static op27no2.parentscope.MyApplication.setScreenshotPermission;

public class AcquireScreenshotPermissionIntent extends Activity {

    private static final int PERMISSION_CODE = 1;
    private MediaProjectionManager mProjectionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("SMALL ON ACTIVITY onCREATE");

        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);

        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);


    }



    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("SMALL ON ACTIVITY RESULT");

        if (1 == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                setScreenshotPermission((Intent) data.clone());
            }
        } else if (Activity.RESULT_CANCELED == resultCode) {
            setScreenshotPermission(null);

            System.out.println("No Permission for SCreenshot");
        }

        finish();
    }



}
