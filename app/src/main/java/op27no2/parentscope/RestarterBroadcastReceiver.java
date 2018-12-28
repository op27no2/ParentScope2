package op27no2.parentscope;

        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.util.Log;
        import android.widget.Toast;

public class RestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        Log.i(RestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        context.startService(new Intent(context, MyService.class));;
        Toast.makeText(context, "ParentScope restarting", Toast.LENGTH_SHORT).show();
        System.out.println("Parentscope restarter");


    }
}
