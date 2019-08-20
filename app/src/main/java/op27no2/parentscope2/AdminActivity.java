package op27no2.parentscope2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.kyleduo.switchbutton.SwitchButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class AdminActivity extends Fragment implements ClickListener, OnProgressBarListener {
    private static final String TAG = "BTPHOTO/Monitored";
    private Spinner deviceSpinner;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialogReceive;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<String> fileNames = new ArrayList<String>();
    private ArrayList<String> filePaths = new ArrayList<String>();
    //private ArrayList<File> fileArray = new ArrayList<File>();
    private ArrayList<FileObject> fileArray = new ArrayList<FileObject>();
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Context mContext;
    private int filesRemaining = 0;
    private Boolean sendingMultiple = false;
    private int caltext = 2;
    private NumberProgressBar bnp;
    private TextView progressText;
    private LinearLayout progressLayout;

    private String retrievalLimit;
    private boolean resetHistory;
    private int calOptions;
    private long dateRangeFar;
    private long dateRangeNear;

        private int retrievalRemaining;
        private boolean useRetrieval = false;
        private boolean isTrasnferring = false;
    private boolean timeOutFlag;

    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.admin_activity, container, false);

        prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();
        mContext = getActivity();


        isTrasnferring = false;


        deviceSpinner = (Spinner) view.findViewById(R.id.deviceSpinner);
        if (MyApplication.pairedDevices != null) {
            final ArrayList<DeviceData> deviceDataList = new ArrayList<DeviceData>();
            for (BluetoothDevice device : MyApplication.pairedDevices) {
                System.out.println("bluetooth devices: "+device.getName());
                deviceDataList.add(new DeviceData(device.getName(), device.getAddress()));
            }

            ArrayAdapter<DeviceData> deviceArrayAdapter = new ArrayAdapter<DeviceData>(getActivity(), android.R.layout.simple_spinner_item, deviceDataList);
            deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deviceSpinner.setAdapter(deviceArrayAdapter);

            System.out.println("check bluetooth "+deviceArrayAdapter);
            int position = 0;
            for(int i=0; i<deviceDataList.size(); i++){
                if(deviceDataList.get(i).getValue() == prefs.getString("bluetooth","default" )){
                    position = i;
                }
            }
            //TODO Can't go by number here, what if it changes dipshit
          //  deviceSpinner.setSelection(prefs.getInt("bluetooth_num",0));
            //TODO SOMETHING WONKY HERE


            deviceSpinner.setSelection(position);

            deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    // Your code here
                    SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                            "PREFS", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("bluetooth",deviceDataList.get(i).getValue());
                    editor.putInt("bluetooth_num",i);
                    editor.putString("identifier",deviceDataList.get(i).toString());
                    editor.commit();
                    populateList();

                }

                public void onNothingSelected(AdapterView<?> adapterView) {
                    return;
                }
            });

            bnp = (NumberProgressBar) view.findViewById(R.id.progress_bar);
            bnp.setOnProgressBarListener(this);
            bnp.setProgress(0);
            progressText = view.findViewById(R.id.progress_text);
            progressText.setText("");
            progressLayout = view.findViewById(R.id.progress_view);
/*            Button clientButton = (Button) view.findViewById(R.id.clientButton);
            clientButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                            System.out.println("sending file");
                            sendFile();
                }
            });*/

            Button pullButton = (Button) view.findViewById(R.id.pullButton);
            pullButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("sending pull request");
                    if(prefs.getBoolean("show_bt_warning",true) == true) {
                        showDialog();
                    }else{
                        sendRequestMessage();
                        progressLayout.setVisibility(View.VISIBLE);
                        timeOutFlag = true;
                        //give 10 seconds to start the process
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("delay test");
                                if(timeOutFlag == true) {
                                    Toast.makeText(getActivity(), "Retrieval Timed Out - Target Device May Be Off or Not Paired", Toast.LENGTH_LONG).show();
                                    hideProgress();
                                }
                            }
                        }, 10000);


                    }
                }
            });

            ImageView clearButton = (ImageView) view.findViewById(R.id.cancel_button);
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO device to cancel processes here too?
                    hideProgress();
                }
            });

        } else {
            Toast.makeText(getActivity(), "No Paired Devices Found, Enable Bluetooth and Pair with Target Device", Toast.LENGTH_LONG).show();
        }








        final SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("requested_pull",true);
        editor.commit();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        // use getActivity() setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(getActivity(),fileArray, filePaths, this);

        // specify an adapter (see also next example)
        mRecyclerView.setAdapter(mAdapter);

        populateList();


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
                        Toast.makeText(getActivity(), "Could not connect to the paired device", Toast.LENGTH_SHORT).show();
                        progressLayout.setVisibility(View.GONE);
                        break;
                    }

                    case zMessageType.SENDING_DATA: {
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage("Receiving Data... This May Take Several Minutes");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        break;
                    }

                    case zMessageType.DATA_SENT_OK: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                       // Toast.makeText(getActivity(), "Photo was sent successfully", Toast.LENGTH_SHORT).show();
                        System.out.println("Photo was sent successfully");
                  //      filesToSend = filesToSend - 1;
                       /* if(sendingMultiple && filesToSend>0) {
                            System.out.println("Sending More: "+filesToSend);
                            sendFile();
                        }*/
                        break;
                    }

                    case zMessageType.DIGEST_DID_NOT_MATCH: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        Toast.makeText(getActivity(), "Photo was sent, but didn't go through correctly", Toast.LENGTH_SHORT).show();
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

                        //TODO SEND 3 settings
                        // WAY TO CANCEL TRANSFER, must send message to other device
                        // 1. Reset retrieval history?
                        // 2. Date Range to Send
                        // 3. File Limit per Retrieval
                      //  mItems.add("Last 12 Hours");
                      //  mItems.add("Last 24 Hours");
                      //  mItems.add("Last Week");
                      //  mItems.add("Custom Range");

                        //make this not local variable at start
                        ArrayList<String> mItems2 = new ArrayList<String>();
                        mItems2.add("1");
                        mItems2.add("2");
                        mItems2.add("3");
                        mItems2.add("4");
                        mItems2.add("5");
                        mItems2.add("10");
                        mItems2.add("20");
                        mItems2.add("50");
                        mItems2.add("100");
                        mItems2.add("No Limit");

                        retrievalLimit = mItems2.get(prefs.getInt("num_option",0));
                        resetHistory = prefs.getBoolean("reset_setting",false);
                        calOptions = prefs.getInt("cala_option",0);
                        dateRangeFar = prefs.getLong("datefar",1);
                        dateRangeNear = prefs.getLong("datenear",1);

                        String details = "Test String/"+(retrievalLimit)+"/"+Boolean.toString(resetHistory)+"/"+Integer.toString(calOptions)+"/"+Long.toString(dateRangeFar)+"/"+Long.toString(dateRangeNear);

                        byte[] myBytes = details.getBytes();

                        // Invoke client thread to send
                        Message mMessage = new Message();
                        mMessage.obj = myBytes;
                        MyApplication.clientThread.incomingHandler.sendMessage(mMessage);



                        break;
                    }

                }
            }
        };

        //THE RECEIVING DEVICE
        MyApplication.serverHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {

                    case zMessageType.DATA_RECEIVED: {
                        //actually doesn't get called until 100%? the data transfer gets called first

                        //message data
                        byte[] myBytes = ((byte[]) message.obj);
                        if (myBytes.length < 100) {
                            String s = new String(myBytes);
                            System.out.println("data received " + s);
                            if (s.equals("no files to receive")) {
                                if (prefs.getString("type", "").equals("admin")) {
                                    hideProgress();
                                    Toast.makeText(mContext, "No Files to Receive with the Chosen Settings", Toast.LENGTH_LONG).show();
                                }
                            }

                        } else {
                            if (prefs.getString("type", "").equals("admin")) {

                            System.out.println("data received, byte length >100 ");


                            if (getActivity() == null)
                                return;

                           /* getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), "receiving picture", Toast.LENGTH_SHORT).show();
                                }
                            });*/


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

                            populateList();
//                            BitmapFactory.Options options = new BitmapFactory.Options();
//                            options.inSampleSize = 2;
//                            Bitmap image = BitmapFactory.decodeByteArray(((byte[]) message.obj), 0, ((byte[]) message.obj).length, options);
//                            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
//                            imageView.setImageBitmap(image);

                            break;
                            }
                        }
                    }

                    case zMessageType.DIGEST_DID_NOT_MATCH: {
                        if(filesRemaining != 0) {
                            Toast.makeText(mContext, "Photo was received, but didn't come through correctly", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }


                    case zMessageType.RECEIVING_DATA: {
                        //set to true with the pull button, if remains true, request has timed out and toasts to user
                        System.out.println("this gets called right");
                        timeOutFlag = false;

                        break;
                    }

                    case zMessageType.FILES_REMAINING: {
                        byte[] myBytes = ((byte[]) message.obj);
                        String remaining = new String(myBytes);
                        filesRemaining = Integer.parseInt(remaining);


                        System.out.println("files remaining "+filesRemaining);


                        if(remaining.equals("0")){
                            Toast.makeText(mContext, "No Files to Receive", Toast.LENGTH_LONG).show();
                        }else {
                            System.out.println("set TEXT?");

                            progressText.setText("Receiving Files: " + filesRemaining + " files remaining");
                        }

                        break;
                    }

                    case zMessageType.DATA_PROGRESS_UPDATE: {
                        // some kind of update
                        byte[] myBytes = ((byte[]) message.obj);
                        String percent = new String(myBytes);

                        System.out.println("percent complete "+percent);
                        if(!percent.equals("") && percent!=null) {
                            int p = Integer.parseInt(percent);

                           // progressDialogReceive.setProgress(p);
                            System.out.println("set progress?"+bnp.getProgress());
                            bnp.setProgress(p);
                            if (p >= 99 && (filesRemaining == 1 || retrievalRemaining ==1)) {
                                hideProgress();
                                isTrasnferring = false;
                            }

                        }

                        break;
                    }

                    case zMessageType.INVALID_HEADER: {
                        Toast.makeText(getActivity(), "Photo was sent, but the header was formatted incorrectly", Toast.LENGTH_SHORT).show();
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
                System.out.println("Starting server thread.  Able to accept photos.");
                MyApplication.serverThread = new zServerThread(MyApplication.adapter, MyApplication.serverHandler);
                MyApplication.serverThread.start();
            }
        }else{

        }




        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MyApplication.PICTURE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "Photo acquired from camera intent");
               /* try {
                    File file = new File(Environment.getExternalStorageDirectory(), MyApplication.TEMP_IMAGE_FILE_NAME);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                    ByteArrayOutputStream compressedImageStream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, MyApplication.IMAGE_QUALITY, compressedImageStream);
                    byte[] compressedImage = compressedImageStream.toByteArray();
                    Log.v(TAG, "Compressed image size: " + compressedImage.length);


                    SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                            "PREFS", Context.MODE_PRIVATE);
                    String videopath = prefs.getString("testfilepath","");
                    System.out.println("filepath: "+videopath);
                    byte[] myBytes = fullyReadFileToBytes(videopath);

                    // Invoke client thread to send
                    Message message = new Message();
                    message.obj = myBytes;
                    MyApplication.clientThread.incomingHandler.sendMessage(message);

                    // Display the image locally
                    ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
                    imageView.setImageBitmap(image);

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }*/
            }
        }
    }

    @Override
    public void onLongClick(int position) {
      //  filePaths.remove(position);
      //  mAdapter.notifyItemRemoved(position);
        System.out.println("listener test");


    }



    class DeviceData {
        public DeviceData(String spinnerText, String value) {
            this.spinnerText = spinnerText;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String toString() {
            return spinnerText;
        }

        String spinnerText;
        String value;
    }


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



    public String getFilePath() {
     //   final String directory = Environment.getExternalStorageDirectory() + File.separator + "ParentScope";
        final String directory = Environment.getExternalStorageDirectory() + File.separator + "Systex"+prefs.getString("identifier","default");
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(getActivity(), "Failed to get External Storage", Toast.LENGTH_SHORT).show();
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

            System.out.println("set file path: "+filePath);

        } else {
            Toast.makeText(mContext, "Failed to create Recordings directory", Toast.LENGTH_SHORT).show();
            System.out.println("Failed to create Recordings directory");
            return null;
        }

        return filePath;
    }
    public String getCurSysDate() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    }



    public void sendFile() {

        final SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        String deviceID = prefs.getString("bluetooth", "fail");
        System.out.println("try to send file deviceID: " + deviceID);

        if (MyApplication.pairedDevices != null) {
            for (BluetoothDevice device : MyApplication.adapter.getBondedDevices()) {
                if (device.getAddress().contains(deviceID)) {
                    System.out.println("starting client thread");
                    if (MyApplication.clientThread != null) {
                        System.out.println("canceled client thread");
                        MyApplication.clientThread.cancel();
                    }
                    MyApplication.clientThread = new zClientThread(device, MyApplication.clientHandler, "photo", 1);
                    MyApplication.clientThread.start();
                }
            }
            //Have to call delayed so client thread has a chance to start
            final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Admin delayed pull message request");

                        //String videopath = prefs.getString("testfilepath", "");
                        if(filePaths.size()>0) {
                            String videopath = filePaths.get(filePaths.size());
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
                            // System.out.println("handlertest: " + MyApplication.clientThread.incomingHandler.toString());
                            MyApplication.clientThread.incomingHandler.sendMessage(message);
                        }
                    }
                }, 3000);


        } else {

            System.out.println("Bluetooth is not enabled or supported on this device");
        }
    }

    public void sendRequestMessage(){
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
                    MyApplication.clientThread = new zClientThread(device, MyApplication.clientHandler, "request",0);
                    MyApplication.clientThread.start();


                }
            }


        } else {

            System.out.println("Bluetooth is not enabled or supported on this device");
        }
    }


    public void showDialog(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_warn, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();
        //alertDialog.getWindow().setLayout(600, 600); //Controlling width and height.
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));

        Button btnAdd2 = (Button) dialogView.findViewById(R.id.dialog_button2);
        Button btnAdd1 = (Button) dialogView.findViewById(R.id.dialog_button1);
        CheckBox cb1 = (CheckBox) dialogView.findViewById(R.id.checkbox);

        btnAdd1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                alertDialog.dismiss();
                if(cb1.isChecked()){
                    edt.putBoolean("show_bt_warning",false);
                    edt.commit();
                }
            }
        });

        btnAdd2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                alertDialog.dismiss();
                sendRequestMessage();
              /*  progressDialogReceive = new ProgressDialog(getActivity());
                progressDialogReceive.setMessage("Receiving Files");
                progressDialogReceive.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialogReceive.setProgress(0);
                progressDialogReceive.setMax(100);
                progressDialogReceive.show();*/

                progressLayout.setVisibility(View.VISIBLE);


                if(cb1.isChecked()){
                    edt.putBoolean("show_bt_warning",false);
                    edt.commit();
                }
            }
        });


        if(!alertDialog.isShowing())
        {
            alertDialog.show();
        }
    }

    public void showDeleteDialog(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();
        //alertDialog.getWindow().setLayout(600, 600); //Controlling width and height.
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));



        RelativeLayout mCal = (RelativeLayout) dialogView.findViewById(R.id.calendar_layout);
        RelativeLayout mNorm = (RelativeLayout) dialogView.findViewById(R.id.normal_layout);
        Button btnAdd2 = (Button) dialogView.findViewById(R.id.dialog_button2);
        Button btnAdd1 = (Button) dialogView.findViewById(R.id.dialog_button1);
        TextView tv1 = (TextView) dialogView.findViewById(R.id.textView1);
        tv1.setText("Delete All Select Items?");


        btnAdd1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertDialog.dismiss();

                //cancel
            }
        });

        btnAdd2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //proceed

                alertDialog.dismiss();
                System.out.println("delete pressed");
                for(int i = fileArray.size()-1; i>=0; i--) {
                    if(fileArray.get(i).getSelected() == 1){
                        deleteFiles(fileArray.get(i).getFile().getAbsolutePath());
                        fileArray.remove(i);
                        filePaths.remove(i);
                        fileNames.remove(i);
                    }
                }
                mAdapter.notifyDataSetChanged();

            }
        });


        if(!alertDialog.isShowing())
        {
            alertDialog.show();
        }


    }

    public void showHelpDialog(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();
        //alertDialog.getWindow().setLayout(600, 600); //Controlling width and height.
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));

        Button btnAdd2 = (Button) dialogView.findViewById(R.id.dialog_button2);
        Button btnAdd1 = (Button) dialogView.findViewById(R.id.dialog_button1);
        TextView tv1 = (TextView) dialogView.findViewById(R.id.textView1);
        tv1.setText("Delete All Select Items?");
        tv1.setText(getResources().getString(R.string.adminhelp));
        btnAdd2.setText("Dismiss");
        btnAdd1.setVisibility(View.GONE);

        btnAdd2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //proceed
                alertDialog.dismiss();
            }
        });


        if(!alertDialog.isShowing())
        {
            alertDialog.show();
        }

        // Get screen width and height in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.8f);
        layoutParams.width = dialogWindowWidth;
        alertDialog.getWindow().setAttributes(layoutParams);


    }

    public void showCalendarDialog(){
        System.out.println("show cal");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_bt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(dialogView);
        AlertDialog alertDialog = alertDialogBuilder.create();
        //alertDialog.getWindow().setLayout(600, 600); //Controlling width and height.
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));

        RelativeLayout mCal = (RelativeLayout) dialogView.findViewById(R.id.calendar_layout);
    //    RelativeLayout mNorm = (RelativeLayout) dialogView.findViewById(R.id.normal_layout);
        LinearLayout linCal = (LinearLayout) dialogView.findViewById(R.id.cal2);
      //  Button btnAdd2 = (Button) dialogView.findViewById(R.id.dialog_button2);
   //     Button btnAdd1 = (Button) dialogView.findViewById(R.id.dialog_button1);
        Button dismissButton = (Button) dialogView.findViewById(R.id.dialog_button3);
        TextView tv2 = (TextView) dialogView.findViewById(R.id.caltext2);
        TextView tv3 = (TextView) dialogView.findViewById(R.id.caltext3);
        SwitchButton mSwitch = dialogView.findViewById(R.id.reset_button);
     //   btnAdd2.setVisibility(View.GONE);
        mCal.setVisibility(View.VISIBLE);
//        mNorm.setVisibility(View.GONE);
        tv2.setText(prefs.getString("tv2","Start Date"));
        tv3.setText(prefs.getString("tv3","End Date"));


        Spinner mSpin = (Spinner) dialogView.findViewById(R.id.spin);
        ArrayList<String> mItems = new ArrayList<String>();
        mItems.add("Last 12 Hours");
        mItems.add("Last 24 Hours");
        mItems.add("Last Week");
        mItems.add("Custom Range");
        ArrayAdapter<String> deviceArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mItems);
        deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpin.setAdapter(deviceArrayAdapter);
        linCal.setVisibility(View.GONE);

        mSpin.setSelection(prefs.getInt("cala_option",0));
        mSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Your code here

                SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                        "PREFS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("cala_option",i);
                editor.commit();
                calOptions = i;


                if(i==3){
                    linCal.setVisibility(View.VISIBLE);
                }else{
                    linCal.setVisibility(View.GONE);
                }

            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        Spinner mSpin2 = (Spinner) dialogView.findViewById(R.id.spin0);
        ArrayList<String> mItems2 = new ArrayList<String>();
        mItems2.add("1");
        mItems2.add("2");
        mItems2.add("3");
        mItems2.add("4");
        mItems2.add("5");
        mItems2.add("10");
        mItems2.add("20");
        mItems2.add("50");
        mItems2.add("100");
        mItems2.add("No Limit");
        ArrayAdapter<String> deviceArrayAdapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mItems2);
        deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpin2.setAdapter(deviceArrayAdapter2);

        mSpin2.setSelection(prefs.getInt("num_option",5));
        mSpin2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Your code here

                SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                        "PREFS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("num_option",i);
                editor.commit();
                retrievalLimit = mItems2.get(i);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        mSwitch.setChecked(prefs.getBoolean("reset_setting",false));
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                edt.putBoolean("reset_setting",b);
                edt.commit();
                resetHistory = b;
            }
        });


        final Calendar myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                if(caltext == 2){
                    String text = sdf.format(myCalendar.getTime());
                    tv2.setText(text);
                    edt.putString("tv2",text);
                    edt.putLong("datefar", myCalendar.getTimeInMillis());
                    edt.commit();
                }else{
                    String text = sdf.format(myCalendar.getTime());
                    tv3.setText(text);
                    edt.putString("tv3",text);
                    edt.putLong("datenear",myCalendar.getTimeInMillis());
                    edt.commit();                }
            }

        };

        tv2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                caltext = 2;
                new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        tv3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                caltext = 3 ;
                new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


  /*      btnAdd1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //proceed
                alertDialog.dismiss();
            }
        });*/

        dismissButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //proceed
                alertDialog.dismiss();
            }
        });

        if(!alertDialog.isShowing())
        {
            alertDialog.show();
        }


        // Get screen width and height in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.9f);
        layoutParams.width = dialogWindowWidth;
        alertDialog.getWindow().setAttributes(layoutParams);

    }

//TODO DO I NEED THIS FOR ADMIN ACTIVITY
    public void populateList(){

        if(deviceSpinner.getAdapter() != null && deviceSpinner.getAdapter().getCount()>0) {
           // final String directory = Environment.getExternalStorageDirectory() + File.separator + "ParentScope";
            final String directory = Environment.getExternalStorageDirectory() + File.separator + "Systex"+prefs.getString("identifier","default");
            System.out.println("admin list directory: "+directory);
            File dir = new File(directory);
            File[] filelist = dir.listFiles();


            if (filelist != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Arrays.sort(filelist, Comparator.comparingLong(File::lastModified));
                }
            }else{
                System.out.println("file list null");
            }

            String[] theNamesOfFiles = new String[0];
            if (filelist != null) {
                theNamesOfFiles = new String[filelist.length];
            }
            fileNames.clear();
            filePaths.clear();
            fileArray.clear();

            for (int i = 0; i < theNamesOfFiles.length; i++) {
                //   theNamesOfFiles[i] = filelist[i].getName();
                if (filelist[i].length() > 0) {
                    fileNames.add(filelist[i].getName());
                    filePaths.add(filelist[i].getAbsolutePath());
                    FileObject mFile = new FileObject(filelist[i],0);
                    fileArray.add(mFile);
                    System.out.println("Files: " + filelist[i].getName());
                }
            }

            //filesToSend = filePaths.size();
            mAdapter.notifyDataSetChanged();

        }
    }



    public void deletePressed(){
        System.out.println("delete pressed");
        showDeleteDialog();
    }
    public void helpPressed(){
        System.out.println("delete pressed");
        showHelpDialog();
    }
    public void calendarPressed(){
        System.out.println("delete pressed");
        showCalendarDialog();
    }


    @Override
    public void onProgressChange(int current, int max) {

    }

    public void deleteFiles(String filepath){

        File file = new File(filepath);
        boolean deleted = file.delete();
        System.out.println("file deleted " + filepath);
    }

    private void hideProgress(){
        bnp.setProgress(0);
        progressText.setText("");
        progressLayout.setVisibility(View.GONE);
    }

}
