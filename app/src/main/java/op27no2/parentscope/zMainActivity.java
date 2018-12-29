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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class zMainActivity extends Activity {
    private static final String TAG = "BTPHOTO/MainActivity";
    private Spinner deviceSpinner;
    private ProgressDialog progressDialog;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zmain);

        //THE SENDING DEVICE DEVICE
        MyApplication.clientHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case zMessageType.READY_FOR_DATA: {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File file = new File(Environment.getExternalStorageDirectory(), MyApplication.TEMP_IMAGE_FILE_NAME);
                        Uri outputFileUri = Uri.fromFile(file);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());
                        startActivityForResult(takePictureIntent, MyApplication.PICTURE_RESULT_CODE);
                        break;
                    }

                    case zMessageType.COULD_NOT_CONNECT: {
                        Toast.makeText(zMainActivity.this, "Could not connect to the paired device", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case zMessageType.SENDING_DATA: {
                        progressDialog = new ProgressDialog(zMainActivity.this);
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
                        Toast.makeText(zMainActivity.this, "Photo was sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case zMessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(zMainActivity.this, "Photo was sent, but didn't go through correctly", Toast.LENGTH_SHORT).show();
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
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }

                        try (FileOutputStream stream = new FileOutputStream(getFilePath())) {
                            stream.write(((byte[]) message.obj));
                        } catch (FileNotFoundException e) {
                            System.out.println("ERROR filenotfound "+e.getMessage());
                            e.printStackTrace();
                        } catch (IOException e) {
                            System.out.println("IO ERROR "+e.getMessage());
                            e.printStackTrace();
                        }

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        Bitmap image = BitmapFactory.decodeByteArray(((byte[]) message.obj), 0, ((byte[]) message.obj).length, options);
                        ImageView imageView = (ImageView) findViewById(R.id.imageView);
                        imageView.setImageBitmap(image);
                        break;
                    }

                    case zMessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(zMainActivity.this, "Photo was received, but didn't come through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case zMessageType.DATA_PROGRESS_UPDATE: {
                        // some kind of update
                        double pctRemaining = 100 - (((double) MyApplication.progressData.remainingSize / MyApplication.progressData.totalSize) * 100);
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(zMainActivity.this);
                            progressDialog.setMessage("Receiving photo...");
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setProgress(0);
                            progressDialog.setMax(100);
                            progressDialog.show();
                        }
                        progressDialog.setProgress((int) Math.floor(pctRemaining));
                        break;
                    }

                    case zMessageType.INVALID_HEADER: {
                        Toast.makeText(zMainActivity.this, "Photo was sent, but the header was formatted incorrectly", Toast.LENGTH_SHORT).show();
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

        if (MyApplication.pairedDevices != null) {
            ArrayList<DeviceData> deviceDataList = new ArrayList<DeviceData>();
            for (BluetoothDevice device : MyApplication.pairedDevices) {
                deviceDataList.add(new DeviceData(device.getName(), device.getAddress()));
            }

            ArrayAdapter<DeviceData> deviceArrayAdapter = new ArrayAdapter<DeviceData>(this, android.R.layout.simple_spinner_item, deviceDataList);
            deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
            deviceSpinner.setAdapter(deviceArrayAdapter);

            Button clientButton = (Button) findViewById(R.id.clientButton);
            clientButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DeviceData deviceData = (DeviceData) deviceSpinner.getSelectedItem();
                    for (BluetoothDevice device : MyApplication.adapter.getBondedDevices()) {
                        if (device.getAddress().contains(deviceData.getValue())) {
                            Log.v(TAG, "Starting client thread");
                            if (MyApplication.clientThread != null) {
                                MyApplication.clientThread.cancel();
                            }
                            MyApplication.clientThread = new zClientThread(device, MyApplication.clientHandler);
                            MyApplication.clientThread.start();
                        }
                    }
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
                try {
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
                }
            }
        }
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

}