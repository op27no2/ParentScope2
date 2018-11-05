package op27no2.parentscope;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    static final String ACTION2 = "android.intent.action.USER_PRESENT";
    @Override
    public void onReceive(Context context, Intent intent) {
        // BOOT_COMPLETED” start Service
        if (intent.getAction().equals(ACTION)) {
            //Service
            System.out.println("Boot Receiver Service Call");
            Toast.makeText(context, "Boot Received", Toast.LENGTH_LONG).show();
            Intent serviceIntent = new Intent(context, MyService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);

            } else {
                context.startService(serviceIntent);
            }

        }
        if (intent.getAction().equals(ACTION2)) {
            //Service
            System.out.println("Boot Receiver Service Call2");
            Toast.makeText(context, "Boot Received", Toast.LENGTH_LONG).show();
            Intent serviceIntent = new Intent(context, MyService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);

            } else {
                context.startService(serviceIntent);
            }

        }
    }
}