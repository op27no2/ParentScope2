package op27no2.parentscope2;

        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.util.Log;
        import android.widget.Toast;

public class RestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        if(prefs.getString("type","").equals("monitored")){

            Log.i(RestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
            context.startService(new Intent(context, MyService.class));
            ;
            Toast.makeText(context, "ParentScope restarting", Toast.LENGTH_SHORT).show();
            System.out.println("Parentscope restarter");

        }
    }
}
