package op27no2.parentscope;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class zServerThread extends Thread {
    private final String TAG = "ServerThread";
    private final BluetoothServerSocket serverSocket;
    private Handler handler;

    public zServerThread(BluetoothAdapter adapter, Handler handler) {
        this.handler = handler;
        BluetoothServerSocket tempSocket = null;
        try {
            tempSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(zConstants.NAME, UUID.fromString(zConstants.UUID_STRING));
        } catch (IOException ioe) {
            Log.e(TAG, ioe.toString());
        }
        serverSocket = tempSocket;
    }

    public void run() {
        BluetoothSocket socket = null;
        if (serverSocket == null)
        {
            Log.d(TAG, "Server socket is null - something went wrong with Bluetooth stack initialization?");
            return;
        }
        while (true) {
            try {
                Log.v(TAG, "Opening new server socket");
                socket = serverSocket.accept();

                try {
                    Log.v(TAG, "Got connection from client.  Spawning new data transfer thread.");
                    zDataTransferThread dataTransferThread = new zDataTransferThread(socket, handler);
                    dataTransferThread.start();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

            } catch (IOException ioe) {
                Log.v(TAG, "Server socket was closed - likely due to cancel method on server thread");
                break;
            }
        }
    }

    public void cancel() {
        try {
            Log.v(TAG, "Trying to close the server socket");
            serverSocket.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}