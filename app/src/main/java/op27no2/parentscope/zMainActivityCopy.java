package op27no2.parentscope;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class zMainActivityCopy extends Activity implements ClickListener {
    private static final String TAG = "BTPHOTO/MonitoredActivity";
    private Spinner deviceSpinner;
    private ProgressDialog progressDialog;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<String> fileNames = new ArrayList<String>();
    private ArrayList<String> filePaths = new ArrayList<String>();
    private ArrayList<FileObject> fileArray = new ArrayList<FileObject>();

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity);


        final String directory = Environment.getExternalStorageDirectory() + File.separator + "ParentScope";
        File dir = new File(directory);
        File[] filelist = dir.listFiles();
        Arrays.sort(filelist, Comparator.comparingLong(File::lastModified));
        String[] theNamesOfFiles = new String[filelist.length];
        for (int i = 0; i < theNamesOfFiles.length; i++) {
         //   theNamesOfFiles[i] = filelist[i].getName();
            if(filelist[i].length()>0) {
                fileNames.add(filelist[i].getName());
                filePaths.add(filelist[i].getAbsolutePath());
                FileObject mFile = new FileObject(filelist[i],0);
                fileArray.add(mFile);
                System.out.println("Files: " + filelist[i].getName());
            }
        }


        final SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("requested_pull",true);
        editor.commit();

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(this,fileArray, filePaths, this);
        mRecyclerView.setAdapter(mAdapter);



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
                        Toast.makeText(zMainActivityCopy.this, "Could not connect to the paired device", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case zMessageType.SENDING_DATA: {
                        progressDialog = new ProgressDialog(zMainActivityCopy.this);
                        progressDialog.setMessage("Sending photo...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        break;
                    }

                    case zMessageType.DATA_SENT_OK: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        Toast.makeText(zMainActivityCopy.this, "Photo was sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case zMessageType.DIGEST_DID_NOT_MATCH: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        Toast.makeText(zMainActivityCopy.this, "Photo was sent, but didn't go through correctly", Toast.LENGTH_SHORT).show();
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

        //THE RECEIVING DEVICE
        MyApplication.serverHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case zMessageType.DATA_RECEIVED: {
                        byte[] myBytes = ((byte[]) message.obj);
                        if(myBytes.length < 100) {
                            String s = new String(myBytes);
                            System.out.println("data received "+s);
                            if(s.equals("Test String")){
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                    progressDialog = null;
                                }
                                sendFile();
                                System.out.println("SHOULD SEND FILE ");

                            }

                        }else {
                            System.out.println("data received, byte length >100 ");


                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(zMainActivityCopy.this, "receiving picture", Toast.LENGTH_SHORT).show();
                                }
                            });

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

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2;
                            Bitmap image = BitmapFactory.decodeByteArray(((byte[]) message.obj), 0, ((byte[]) message.obj).length, options);
                            ImageView imageView = (ImageView) findViewById(R.id.imageView);
                            imageView.setImageBitmap(image);

                            break;
                        }
                    }

                    case zMessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(zMainActivityCopy.this, "Photo was received, but didn't come through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case zMessageType.DATA_PROGRESS_UPDATE: {
                        // some kind of update
                        double pctRemaining = 100 - (((double) MyApplication.progressData.remainingSize / MyApplication.progressData.totalSize) * 100);
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(zMainActivityCopy.this);
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
                        Toast.makeText(zMainActivityCopy.this, "Photo was sent, but the header was formatted incorrectly", Toast.LENGTH_SHORT).show();
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

        if (MyApplication.pairedDevices != null) {
            final ArrayList<DeviceData> deviceDataList = new ArrayList<DeviceData>();
            for (BluetoothDevice device : MyApplication.pairedDevices) {
                deviceDataList.add(new DeviceData(device.getName(), device.getAddress()));
            }

            ArrayAdapter<DeviceData> deviceArrayAdapter = new ArrayAdapter<DeviceData>(this, android.R.layout.simple_spinner_item, deviceDataList);
            deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
            deviceSpinner.setAdapter(deviceArrayAdapter);

            deviceSpinner.setSelection(prefs.getInt("bluetooth_num",0));
            deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    // Your code here
                    SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences(
                            "PREFS", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("bluetooth",deviceDataList.get(i).getValue());
                    editor.putInt("bluetooth_num",i);
                    editor.commit();
                }

                public void onNothingSelected(AdapterView<?> adapterView) {
                    return;
                }
            });


/*            Button clientButton = (Button) findViewById(R.id.clientButton);
            clientButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                            System.out.println("sending file");
                            sendFile();
                }
            });*/

            Button pullButton = (Button) findViewById(R.id.pullButton);
            pullButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("sending pull request");

                    sendRequestMessage();
                }
            });

        } else {
            Toast.makeText(this, "Bluetooth is not enabled or supported on this device", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageBitmap(image);

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }*/
            }
        }
    }

    @Override
    public void onLongClick(int position) {

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
                    MyApplication.clientThread = new zClientThread(device, MyApplication.clientHandler, "photo", 0);
                    MyApplication.clientThread.start();
                }
            }
            //Have to call delayed so client thread has a chance to start
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.out.println("DELAYED FILE TRANSFER TEST");

                    String videopath = prefs.getString("testfilepath", "");
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
            }, 15000);


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


}