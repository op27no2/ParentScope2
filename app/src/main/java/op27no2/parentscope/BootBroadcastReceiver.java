package op27no2.parentscope;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    static final String ACTION2 = "android.intent.action.USER_PRESENT";
    static final String ACTION3 = "android.intent.action.QUICKBOOT_POWERON";
    static final String ACTION4 = "com.htc.intent.action.QUICKBOOT_POWERON";
    static final String ACTION5 = "android.intent.action.LOCKED_BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {


        SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        if(prefs.getString("type","").equals("monitored")){

            // BOOT_COMPLETED” start Service
            if (intent.getAction().equals(ACTION)) {
                //Service
                System.out.println("ParentScope Boot Receiver Service Call");
                Toast.makeText(context, "Boot Received", Toast.LENGTH_LONG).show();
                Intent serviceIntent = new Intent(context, MyService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);

                } else {
                    context.startService(serviceIntent);
                }

            }
            if (intent.getAction().equals(ACTION2) || intent.getAction().equals(ACTION3) || intent.getAction().equals(ACTION4) || intent.getAction().equals(ACTION5)) {
                //Service
                System.out.println("ParentScope Boot Receiver Service Call2");
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


}