package op27no2.parentscope;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
//THE SENDING DEVICE
public class zClientThread extends Thread {
    private final String TAG = "btxfr/ClientThread";
    private final BluetoothSocket socket;
    private final Handler handler;
    private BluetoothDevice device;
    public Handler incomingHandler;
    private String task;
    private static UUID MY_UUID = UUID.fromString(zConstants.UUID_STRING);
    private int filesToSend;

    public zClientThread(BluetoothDevice device, Handler handler, String task, int files) {
        BluetoothSocket tempSocket = null;
        this.handler = handler;
        this.task = task;
        this.device = device;
        this.filesToSend = files;

        try {
            tempSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        this.socket = tempSocket;
        cancel();

    }

    @SuppressLint("HandlerLeak")
    public void run() {
        try {

            Log.v(TAG, "Opening client socket");

            socket.connect();

            Log.v(TAG, "Connection established");

        } catch (IOException ioe) {
            handler.sendEmptyMessage(zMessageType.COULD_NOT_CONNECT);
            Log.e(TAG, ioe.toString());
            try {
                socket.close();
            } catch (IOException ce) {
                Log.e(TAG, "Socket close exception: " + ce.toString());
            }
        }

        Looper.prepare();

        System.out.println("CLIENT THREAD create handler");

        incomingHandler = new Handler(){
            @Override
            public void handleMessage(Message message)
            {
                if (message.obj != null)
                {
                    Log.v(TAG, "Handle received data to send");
                    byte[] payload = (byte[])message.obj;

                    try {
                        handler.sendEmptyMessage(zMessageType.SENDER_STARTED);
                        OutputStream outputStream = socket.getOutputStream();

                        // Send the header control first
                        outputStream.write(filesToSend);
                        outputStream.write(zConstants.HEADER_MSB);
                        outputStream.write(zConstants.HEADER_LSB);

                        // write size
                        outputStream.write(zUtils.intToByteArray(payload.length));

                        // write digest
                        byte[] digest = zUtils.getDigest(payload);
                        outputStream.write(digest);

                        // now write the data
                        outputStream.write(payload);
                        outputStream.flush();

                        Log.v("wut", "Data sent.  Waiting for return digest as confirmation");
                        InputStream inputStream = socket.getInputStream();
                        byte[] incomingDigest = new byte[16];
                        int incomingIndex = 0;

                        try {
                            while (true) {
                                byte[] header = new byte[1];
                                inputStream.read(header, 0, 1);
                                incomingDigest[incomingIndex++] = header[0];
                                if (incomingIndex == 16) {
                                    if (zUtils.digestMatch(payload, incomingDigest)) {
                                        Log.v(TAG, "Digest matched OK.  Data was received OK.");
                                        zClientThread.this.handler.sendEmptyMessage(zMessageType.DATA_SENT_OK);

                                    } else {
                                        Log.e(TAG, "Digest did not match.  Might want to resend.");
                                        zClientThread.this.handler.sendEmptyMessage(zMessageType.DIGEST_DID_NOT_MATCH);
                                    }

                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, ex.toString());
                        }

                        Log.v(TAG, "Closing the client socket.");
                        socket.close();

                    } catch (Exception e) {
                        Log.e(TAG, e.toString());

                    }

                }
            }
        };

        handler.sendEmptyMessage(zMessageType.READY_FOR_DATA);
        if(task.equals("request")){
            handler.sendEmptyMessage(zMessageType.REQUEST);
        }
        Looper.loop();
    }

    public void cancel() {
        try {
            if (socket.isConnected()) {
                socket.getInputStream().close();
                socket.getOutputStream().close();
                socket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void deleteFiles(String filepath){

        File file = new File(filepath);
        boolean deleted = file.delete();
        System.out.println("file deleted " +deleted+" "+ filepath);
    }


}