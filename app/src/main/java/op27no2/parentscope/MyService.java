package op27no2.parentscope;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class MyService extends Service {
    Handler mHandler2;
    Runnable mRunnable;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjection mMediaProjection;
    private static final String TAG = "ServiceTag";


    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static int DISPLAY_WIDTH = 960;
    private static int DISPLAY_HEIGHT = 1280;
    private MyService.MediaProjectionCallback mMediaProjectionCallback;
    private ToggleButton mToggleButton;
    private MyService myService;
    private BluetoothChatService mChatService = null;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private ServiceInterface serviceCallbacks;
    private Boolean isRecording = false;
    private String mConnectedDeviceName = null;

    private Boolean isSending = false;
    //from zMainACtivity
    private ProgressDialog progressDialog;
    private SharedPreferences prefs;
    SharedPreferences.Editor edt;

    private ArrayList<String> filePaths = new ArrayList<String>();
    private ArrayList<String> alreadySent = new ArrayList<String>();
    private int filesToSend = 0;
    private Boolean sendingMultiple = false;

    private int qMode;
    private int dayCount = 0;
    private int dayStored;
    private int totalCount;
    private int totalStored;
    private int frequency;
    private int duration;
    private int duration_msec;
    private boolean deleteafter;
    private boolean recordmic;
    private BroadcastReceiver mReceiver;

    private String retrievalLimit;
    private boolean resetHistory;
    private int calOptions;
    private long datefar;
    private long datenear;





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
        System.out.println("Heron service onCreate");
        prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();
        populateList();

        qMode = prefs.getInt("quality_setting",0);
        dayCount = prefs.getInt("stored_today",0);
        totalCount = prefs.getInt("stored_total",0);
        dayStored = prefs.getInt("stored_setting",0);
        totalStored = prefs.getInt("total_setting",0);
        frequency = prefs.getInt("req_setting",100);
        duration = prefs.getInt("duration_setting",6);
        switch(duration){
            case 0:
                duration_msec = 300000;
                break;
            case 1:
                duration_msec = 120000;
                break;
            case 2:
                duration_msec = 60000;
                break;
            case 3:
                duration_msec = 30000;
                break;
            case 4:
                duration_msec = 15000;
                break;
            case 5:
                duration_msec = 10000;
                break;
            case 6:
                duration_msec = 5000;
                break;
            default:
                duration_msec = 5000;

        }

        duration = prefs.getInt("duration_setting",7);
        deleteafter = prefs.getBoolean("delete_setting",false);
        recordmic = prefs.getBoolean("mic_setting",false);



        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String date = sdf.format(System.currentTimeMillis());
        if(!date.equals(prefs.getString("last_day",""))){
            dayCount = 0;
        }
        edt.putString("last_day", date);
        edt.commit();


        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);


            Intent showTaskIntent = new Intent(getApplicationContext(), BriefActivity.class);
            showTaskIntent.setAction(Intent.ACTION_MAIN);
            showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent contentIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    showTaskIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Currently Active")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .build();


            startForeground(1, notification);

            //test code to try to cancel notification
    /*        final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {


                    System.out.println("TRY TO CANCEL NOTIFICATIONS");

                }
            }, 10000);*/


        }
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        DISPLAY_WIDTH = width;
        DISPLAY_HEIGHT = height;


        //receiver to update settings

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));





      //  Toast.makeText(this, "Service onCreate method.", Toast.LENGTH_LONG).show();
        super.onCreate();
    }


    @SuppressLint("HandlerLeak")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Heron service started");
        Toast.makeText(this, "Heron Started", Toast.LENGTH_SHORT).show();



        MyApplication.getScreenshotPermission();
        mMediaProjection = MyApplication.mMediaProjection;
        mMediaProjectionCallback = new MyService.MediaProjectionCallback();

        System.out.println("service media? "+ mMediaProjection);

        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        window.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mChatService = new BluetoothChatService(this, mHandler);
        mChatService.start();


        mHandler2 = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {

                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /*        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoverableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(
                            BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(discoverableIntent);
                }*/

              // System.out.println("ParentScope Foreground Task: "+printForegroundTask());
                printForegroundTask();

                mHandler2.postDelayed(mRunnable, 2000); //100 ms you should do it 4000
            }
        };

        mHandler2.postDelayed(mRunnable, 0);

        //From AdminActivity


        //THE SENDING DEVICE DEVICE
        MyApplication.clientHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case zMessageType.READY_FOR_DATA: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        System.out.println("client/sender ready for data");

                   /*     Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File file = new File(Environment.getExternalStorageDirectory(), MyApplication.TEMP_IMAGE_FILE_NAME);
                        Uri outputFileUri = Uri.fromFile(file);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());*/
                        //                  startActivityForResult(takePictureIntent, MyApplication.PICTURE_RESULT_CODE);
                        break;
                    }

                    case zMessageType.COULD_NOT_CONNECT: {
                        Toast.makeText(getApplicationContext(), "Could not connect to the paired device", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case zMessageType.SENDING_DATA: {
                        progressDialog = new ProgressDialog(getApplicationContext());
                        progressDialog.setMessage("Receiving Data... This May Take Several Minutes");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                        progressDialog.show();
                        break;
                    }

                    case zMessageType.DATA_SENT_OK: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        // Toast.makeText(getActivity(), "Photo was sent successfully", Toast.LENGTH_SHORT).show();
                        System.out.println("Photo was sent successfully");

                        //delete or track if has been deleted
                        if(prefs.getBoolean("delete_setting",false)) {
                            deleteFiles(filePaths.get(filePaths.size() - filesToSend));
                        }else{
                            Set<String> set = prefs.getStringSet("already_sent", null);
                            if(set == null){
                                set = new HashSet<String>();
                            }
                            String path = filePaths.get(filePaths.size() - filesToSend);
                            System.out.println("filepaths size:" + filePaths.size()+ " filesToSend: "+filesToSend);
                            System.out.println("filepath: " + path);
                           // System.out.println("filepath sep index: " + path.lastIndexOf(File.separator)+1);
                            System.out.println("filepath sep index: " + path.lastIndexOf("/"));
                            System.out.println("substring is: "+path.substring(path.lastIndexOf(File.separator)+1));
                            //get filename from path
                            set.add(path.substring(path.lastIndexOf(File.separator)+1));
                            edt.putStringSet("already_sent", set);
                            edt.commit();
                        }

                        filesToSend = filesToSend - 1;
                        if(sendingMultiple && filesToSend>0) {
                            System.out.println("Sending More: "+filesToSend);
                            sendFile();
                        }
                        break;
                    }

                    case zMessageType.DIGEST_DID_NOT_MATCH: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        Toast.makeText(getApplicationContext(), "Photo was sent, but didn't go through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case zMessageType.PHOTO: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }

                        break;
                    }
                    case zMessageType.REQUEST: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }

                        System.out.println("SIMPLE MESSAGE TEST");

                        byte[] myBytes = "Test String".getBytes();

                        // Invoke client thread to send
                        Message mMessage = new Message();
                        mMessage.obj = myBytes;
                        MyApplication.clientThread.incomingHandler.sendMessage(mMessage);



                        break;
                    }

                }
            }
        };

        //RECEIVE MESSAGE FROM ADMIN DEVICE
        MyApplication.serverHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case zMessageType.DATA_RECEIVED: {
                        byte[] myBytes = ((byte[]) message.obj);
                        // SHORT MESSAGE PROVIDES SETTINGS AND SIGNALS SERVICE TO SEND PHOTOS
                        if(myBytes.length < 100) {
                            if (prefs.getString("type", "").equals("monitored")) {

                                Toast.makeText(getApplicationContext(), "Received Prompt Message", Toast.LENGTH_LONG).show();

                            String s = new String(myBytes);
                            System.out.println("received string: "+s);
                            int[] indecies = {0,0,0,0,0,0};
                            for(int i=0;i<5;i++){
                                indecies[i+1] = s.indexOf("/",indecies[i]+1);
                            }
                            System.out.println("indecies: "+indecies[0]+" "+indecies[1]+" "+indecies[2]+" "+indecies[3]+" "+indecies[4]+" "+indecies[5]);
                            retrievalLimit =(s.substring(indecies[1]+1,indecies[2]));
                            resetHistory = Boolean.parseBoolean(s.substring(indecies[2]+1,indecies[3]));
                            calOptions = Integer.parseInt(s.substring(indecies[3]+1, indecies[4]));
                            datefar = Long.parseLong(s.substring(indecies[4]+1, indecies[5]));
                            datenear = Long.parseLong(s.substring(indecies[5]+1));
                            System.out.println("retrieval limit: "+ retrievalLimit);
                            System.out.println("reset history: "+ resetHistory);
                            System.out.println("caloptions : "+ calOptions);
                            System.out.println("datefar : "+ datefar);
                            System.out.println("datenear : "+ datenear);




                            System.out.println("data received "+s);
                            if(s.substring(0,11).equals("Test String")) {
                                populateList();

                                if(retrievalLimit.equals("No Limit")){
                                    //do nothing, default filesToSend from file list
                                }else{
                                    int limit = Integer.parseInt(retrievalLimit);
                                    if(filesToSend > limit) {
                                        filesToSend = limit;
                                    }
                                    System.out.println("limit set to: "+filesToSend);
                                }


                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                    progressDialog = null;
                                }
                                if (filesToSend > 1) {
                                    sendingMultiple = true;
                                }
                                if(filesToSend>0) {
                                    sendFile();
                                }else{
                                    //send message there are no files to receive
                                    sendString("no files to receive");
                                }
                                System.out.println("SHOULD SEND FILES "+filesToSend);
                            }
                        }


                        }else {

                            //TODO send message to close retrieving files popup
                            //service should not be receiving the large files.
                  /*          if (prefs.getString("type", "").equals("admin")) {
                                System.out.println("data received, byte length >100 ");


                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                    progressDialog = null;
                                }

                                try (FileOutputStream stream = new FileOutputStream(getFilePath())) {
                                    stream.write(((byte[]) message.obj));
                                    stream.close();
                                } catch (FileNotFoundException e) {
                                    System.out.println("ERROR filenotfound " + e.getMessage());
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    System.out.println("IO ERROR " + e.getMessage());
                                    e.printStackTrace();
                                }

                                break;
                            }*/
                        }
                    }

                    case zMessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(getApplicationContext(), "Photo was received, but didn't come through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case zMessageType.DATA_PROGRESS_UPDATE: {
                        // some kind of update
                        double pctRemaining = 100 - (((double) MyApplication.progressData.remainingSize / MyApplication.progressData.totalSize) * 100);
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(getApplicationContext());
                            progressDialog.setMessage("Receiving photo...");
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setProgress(0);
                            progressDialog.setMax(100);
//                            progressDialog.show();
                        }
                        System.out.println("percent remaining "+pctRemaining);

                        progressDialog.setProgress((int) Math.floor(pctRemaining));
                        break;
                    }

                    case zMessageType.INVALID_HEADER: {
                        Toast.makeText(getApplicationContext(), "Photo was sent, but the header was formatted incorrectly", Toast.LENGTH_SHORT).show();
                        break;
                    }


                    case zMessageType.RECEIVED_CONNECTION: {
                        System.out.println("MAIN ACTIVITY MESSAGE TEST");


                        break;
                    }
                }
            }
        };


        if (MyApplication.pairedDevices != null) {
            if (MyApplication.serverThread == null) {
                Log.v(TAG, "Starting server thread.  Able to accept photos.");
                MyApplication.serverThread = new zServerThread(MyApplication.adapter, MyApplication.serverHandler);
                MyApplication.serverThread.start();
            }
        }





        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;

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

     //ENABLE THIS FOR PERIODIC REPORTING
     //   Log.e("adapter", "Current App in foreground is: " + currentApp);
     //   System.out.println(isRecording.toString() +" "+ dayCount +" "+ dayStored +" "+ totalCount +" "+ totalStored+" "+frequency);


        //TESTING SCREEN RECORD WITH snapchat open
       // if(currentApp.equals("com.snapchat.android") && isRecording==false && (dayCount <= dayStored) && (totalCount <= totalStored) && (getRandomNumber(0,100) <= frequency)){
        if(currentApp.equals("com.snapchat.android") && isRecording==false){
            dayCount = dayCount + 1;
            totalCount = totalCount + 1;
            edt.putInt("stored_today",dayCount);
            edt.putInt("stored_total",totalCount);
            edt.commit();


            //everytime we capture get current date, if its not the same as last capture or service start, reset max per day
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String date = sdf.format(System.currentTimeMillis());
            if(!date.equals(prefs.getString("last_day",""))){
                dayCount = 0;
            }
            edt.putString("last_day", date);
            edt.commit();

            isRecording = true;
            System.out.println("Launching Record Activity");
            mMediaProjection = MyApplication.mMediaProjection;
            mMediaProjectionCallback = new MyService.MediaProjectionCallback();

            System.out.println("service media2? "+ mMediaProjection);


            initRecorder();
            prepareRecorder();
            startRecord();

            //don't think I need this to record
  /*          Intent dialogIntent = new Intent(this, RecordActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(dialogIntent);
*/
            System.out.println("check callbacks");

            //I think taking this out stops record function
            if (serviceCallbacks != null) {
                serviceCallbacks.start();
                System.out.println("START RECORD");
            }



            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRecord();
                    isRecording = false;
                    System.out.println("STAHP RECORD");

                }
            }, duration_msec);

        }

        //TESTING FILE TRANSFER WITH ISNTAGRAM OPEN
/*        if(currentApp.equals("com.instagram.android")&&isSending==false) {
            isSending = true;

            final SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                    "PREFS", Context.MODE_PRIVATE);
            String deviceID = prefs.getString("bluetooth","fail");
            System.out.println("try to send file deviceID: "+deviceID);

            if (MyApplication.pairedDevices != null) {
                for (BluetoothDevice device : MyApplication.adapter.getBondedDevices()) {
                    if (device.getAddress().contains(deviceID)) {
                        System.out.println("starting client thread");
                        if (MyApplication.clientThread != null) {
                            System.out.println("canceled client thread");
                            MyApplication.clientThread.cancel();
                        }
                        MyApplication.clientThread = new zClientThread(device, MyApplication.clientHandler, "photo");
                        MyApplication.clientThread.start();
                    }
                }
                //Have to call delayed so client theread has a chance to start
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("DELAYED FILE TRANSFER TEST");


                String videopath = prefs.getString("testfilepath","");
                System.out.println("filepath: "+videopath);
                byte[] myBytes = new byte[0];
                try {
                    myBytes = fullyReadFileToBytes(videopath);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("service io exception: "+e.getMessage());
                }

                // Invoke client thread to send
                Message message = new Message();
                message.obj = myBytes;
                System.out.println("handlertest: "+ MyApplication.clientThread.incomingHandler.toString());
                MyApplication.clientThread.incomingHandler.sendMessage(message);
                    }
                }, 3000);

                final Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isSending = false;
                    }
                }, 10000);

            } else {
                Toast.makeText(this, "Bluetooth is not enabled or supported on this device", Toast.LENGTH_LONG).show();
                System.out.println("Bluetooth is not enabled or supported on this device");
            }
        }*/


        return currentApp;
    }

    @Override
    public void onDestroy() {
        System.out.println("Heron service destroyed");
        Toast.makeText(this, "Heron Service Destroyed", Toast.LENGTH_SHORT).show();

     /*   Intent intent = new Intent("restartApps");
        sendBroadcast(intent);
*/
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());

        restartService.setPackage(getPackageName());

        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android

        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +1000, restartServicePI);


        if(mHandler2 != null && mRunnable != null) {
            mHandler2.removeCallbacks(mRunnable);
        }

/*
        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(this.ALARM_SERVICE);
        Intent i = new Intent(this, MyService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, i, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10, pendingIntent);


        Intent broadcastIntent = new Intent("com.parentscope.ReceiverDem");
        sendBroadcast(broadcastIntent);*/

        super.onDestroy();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("ParentScope Task Removed");
        // TODO Auto-generated method stub
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());

        restartService.setPackage(getPackageName());

        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android

        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +1000, restartServicePI);

    }



    public void startRecord() {
        System.out.println("start record");

        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();

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
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MonitoredActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null /*Handler*/);
    }




    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            System.out.println("Recording STOPPED FROM PROJECTION CALLBACK");
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    private void prepareRecorder() {
        System.out.println("Prepare recorder");
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }


    public String getFilePath() {
      // final String directory = Environment.getExternalStorageDirectory() + File.separator + "ParentScope";

        //this is where the files will be saved after recording. The code in the sending section in this service must match this location

        //the code in getFile in AdminActivity on the receiving device can place the files anywhere,but populate list in the AdminActivity must look there.

        final String directory = Environment.getExternalStorageDirectory() + File.separator + "Systex";

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

            SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                    "PREFS", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("testfilepath",filePath);
            editor.commit();
            System.out.println("set file path: "+filePath);

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
            if(recordmic) {
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            }
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            switch(qMode){
                case 0:
                    mMediaRecorder.setVideoEncodingBitRate(3000000);
                    mMediaRecorder.setCaptureRate(20);
                    mMediaRecorder.setVideoFrameRate(20);
                    break;

                case 1:
                    mMediaRecorder.setVideoEncodingBitRate(2000000);
                    mMediaRecorder.setCaptureRate(10);
                    mMediaRecorder.setVideoFrameRate(10);
                    break;

                case 2:
                    mMediaRecorder.setVideoEncodingBitRate(1000000);
                    mMediaRecorder.setCaptureRate(5);
                    mMediaRecorder.setVideoFrameRate(5);
                    break;

                case 3:
                    mMediaRecorder.setVideoEncodingBitRate(3000000);
                    mMediaRecorder.setCaptureRate(1);
                    mMediaRecorder.setVideoFrameRate(1);
                    break;

                default:
                    mMediaRecorder.setVideoEncodingBitRate(3000000);
                    mMediaRecorder.setCaptureRate(20);
                    mMediaRecorder.setVideoFrameRate(20);
            }

            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setOutputFile(getFilePath());
        }
    }


    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressLint("StringFormatInvalid")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(),
                                    mConnectedDeviceName, Toast.LENGTH_SHORT)
                                    .show();
                      //      mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.title_connecting), Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.title_not_connected), Toast.LENGTH_SHORT)
                                    .show();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
              //      mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
              //              + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            "toasting", Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    };




    //MORE CODE FROM ZMAINACTIVITY
    byte[] fullyReadFileToBytes(String filepath) throws IOException {
        File f = new File(filepath);
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis= new FileInputStream(f);;
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        }  catch (IOException e){
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }



    public void populateList(){

            final String directory = Environment.getExternalStorageDirectory() + File.separator + "Systex";
            System.out.println("directory = "+directory);
            File dir = new File(directory);
            File[] filelist = dir.listFiles();

            if (filelist != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Arrays.sort(filelist, Comparator.comparingLong(File::lastModified));
                }
            }

            String[] theNamesOfFiles = new String[0];
            if (filelist != null) {
                System.out.println("Number of Files: " + filelist.length);
                theNamesOfFiles = new String[filelist.length];
            }else{
                System.out.println("null list of files");
            }

        Set<String> set = prefs.getStringSet("already_sent", null);

        filePaths.clear();

        System.out.println("date far: "+datefar);
        System.out.println("date near: "+datenear);

        //TODO something wrong here? names of files not correct? filepaths not being populated...
        for (int i = 0; i < theNamesOfFiles.length; i++) {
                //   theNamesOfFiles[i] = filelist[i].getName();
                if (filelist[i].length() > 0) {
                    //Check date of file.
                    System.out.println("modified times: "+filelist[i].lastModified());



                    //check if we have already sent the file, if not, add to filePaths that are sendable
                  //  if(set!=null && !set.contains(filelist[i].getName())) {

                    if(filelist[i].lastModified()>datefar && filelist[i].lastModified() < datenear) {
                        filePaths.add(filelist[i].getAbsolutePath());
                        System.out.println("Files: " + filePaths);
                    }

                  //  }
                }
            }

            System.out.println("filestosend SET: "+filesToSend);
            filesToSend = filePaths.size();
    }


    public void sendFile() {
       // populateList();

        final SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        String deviceID = prefs.getString("bluetooth", "fail");
        System.out.println("try to send file deviceID: " + deviceID);

        if (MyApplication.pairedDevices != null) {
            boolean deviceFound = false;
            for (BluetoothDevice device : MyApplication.adapter.getBondedDevices()) {
                if (device.getAddress().contains(deviceID)) {
                    deviceFound = true;
                    System.out.println("starting client thread");
                    if (MyApplication.clientThread != null) {
                        System.out.println("canceled client thread");
                        MyApplication.clientThread.cancel();
                    }
                    MyApplication.clientThread = new zClientThread(device, MyApplication.clientHandler, "photo", filesToSend);
                    MyApplication.clientThread.start();
                }
            }
            if(deviceFound == true) {
                //Have to call delayed so client thread has a chance to start
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("DELAYED FILE TRANSFER TEST " + filePaths);

                        if (filePaths.size() > 0) {
                            String videopath = filePaths.get(filePaths.size() - filesToSend);
                            System.out.println("filepath: " + videopath);
                            byte[] myBytes = new byte[0];
                            try {
                                myBytes = fullyReadFileToBytes(videopath);
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("service io exception: " + e.getMessage());
                            }

                            // Invoke client thread to send
                            Message message = new Message();
                            message.obj = myBytes;
                            System.out.println("handlertest: " + MyApplication.clientThread.incomingHandler.toString());
                            MyApplication.clientThread.incomingHandler.sendMessage(message);
                        }
                    }
                }, 3000);
            }else{
                Toast.makeText(this, "Heron - Select Correct Admin Device and Pair", Toast.LENGTH_LONG).show();
            }

        } else {

            System.out.println("Bluetooth is not enabled or supported on this device");
        }
    }


    private int getRandomNumber(int min,int max) {
        return (new Random()).nextInt((max - min) + 1) + min;
    }

    public void deleteFiles(String filepath){
        File file = new File(filepath);
        boolean deleted = file.delete();
        System.out.println("file deleted "+deleted+" " + filepath);
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            System.out.println("Got message: " + message);
        }
    };

    private void sendString(String message) {
        byte[] myBytes = message.getBytes();

        // Invoke client thread to send
        Message mMessage = new Message();
        mMessage.obj = myBytes;
        MyApplication.clientThread.incomingHandler.sendMessage(mMessage);

    }



    }