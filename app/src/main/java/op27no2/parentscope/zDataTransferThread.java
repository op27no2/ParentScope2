package op27no2.parentscope;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

class zDataTransferThread extends Thread {
    private final String TAG = "DataTransferThread";
    private final BluetoothSocket socket;
    private Handler handler;

    public zDataTransferThread(BluetoothSocket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            boolean waitingForHeader = true;
            ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();
            byte[] headerBytes = new byte[23];
            byte[] digest = new byte[16];
            int headerIndex = 0;
            zProgressData progressData = new zProgressData();

            System.out.println("zDataTransferThread receiving data");
            handler.sendEmptyMessage(zMessageType.RECEIVING_DATA);

            while (true) {
                if (waitingForHeader) {
                    System.out.println("zDataTransferThread waiting for header");
                    //setting first header byte as files remaining...
                    byte[] header = new byte[1];
                    inputStream.read(header, 0, 1);
                    Log.v(TAG, "Received Header Byte: " + header[0]);

                    headerBytes[headerIndex++] = header[0];

                //    if (headerIndex == 22) {
                    if (headerIndex == 23) {
                        System.out.println("zDataTransferThread header index is 22 ");

                //        if ((headerBytes[1] == zConstants.HEADER_MSB) && (headerBytes[2] == zConstants.HEADER_LSB)) {
                        if ((headerBytes[1] == zConstants.HEADER_MSB) && (headerBytes[2] == zConstants.HEADER_LSB)) {
                            Log.v(TAG, "Header Received.  Now obtaining length");
                            System.out.println("zDataTransferThread header msb");
                            int i1 = headerBytes[0];
                            sendFilesRemaining(Integer.toString(i1));

                          //  byte[] dataSizeBuffer = Arrays.copyOfRange(headerBytes, 2, 6);
                            byte[] dataSizeBuffer = Arrays.copyOfRange(headerBytes, 3, 7);
                            progressData.totalSize = zUtils.byteArrayToInt(dataSizeBuffer);
                            progressData.remainingSize = progressData.totalSize;
                            System.out.println("Bytes Total Size : "+progressData.totalSize);

                            Log.v(TAG, "Data size: " + progressData.totalSize);
                           // digest = Arrays.copyOfRange(headerBytes, 6, 22);
                            digest = Arrays.copyOfRange(headerBytes, 7, 23);
                            waitingForHeader = false;
                            sendProgress(progressData, "");
                        } else {
                            System.out.println("zDataTransferThread incorrect header closing socket");

                            Log.e(TAG, "Did not receive correct header.  Closing socket");
                            socket.close();
                            handler.sendEmptyMessage(zMessageType.INVALID_HEADER);
                            break;
                        }
                    }

                } else {
                    // Read the data from the stream in chunks
                //    System.out.println("zDataTransferThread reading data");

                    byte[] buffer = new byte[zConstants.CHUNK_SIZE];
                    Log.v(TAG, "Waiting for data.  Expecting " + progressData.remainingSize + " more bytes.");
                    int bytesRead = inputStream.read(buffer);
                    Log.v(TAG, "Read " + bytesRead + " bytes into buffer");
                    System.out.println("zDataTransferThread Bytes read: "+bytesRead);
                    dataOutputStream.write(buffer, 0, bytesRead);
                    progressData.remainingSize -= bytesRead;
                    String perc = Integer.toString(100 - (int) Math.floor(100*(((double)progressData.remainingSize / (double) progressData.totalSize))));
                    System.out.println("data perc complete: "+perc);
                    sendProgress(progressData, perc);

                    if (progressData.remainingSize <= 0) {
                        Log.v(TAG, "Expected data has been received.");
                        break;
                    }
                }
            }

            // check the integrity of the data
            final byte[] data = dataOutputStream.toByteArray();

            if (zUtils.digestMatch(data, digest)) {
                Log.v(TAG, "Digest matches OK.");
                Message message = new Message();
                message.obj = data;
                message.what = zMessageType.DATA_RECEIVED;
                handler.sendMessage(message);

                // Send the digest back to the client as a confirmation
                Log.v(TAG, "Sending back digest for confirmation");
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(digest);

            } else {
                Log.e(TAG, "Digest did not match.  Corrupt transfer?");
                handler.sendEmptyMessage(zMessageType.DIGEST_DID_NOT_MATCH);
            }

            Log.v(TAG, "Closing server socket");
            socket.close();

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }
    }

    private void sendProgress(zProgressData progressData, String percent) {
        System.out.println("zDataTransferThread sending Progress");

        Message message = new Message();
        byte[] myBytes = percent.getBytes();
        message.obj = myBytes;
        message.what = zMessageType.DATA_PROGRESS_UPDATE;
        handler.sendMessage(message);
    }

    private void sendFilesRemaining(String files) {
        System.out.println("zDataTransferThread sending Progress");

        Message message = new Message();
        byte[] myBytes = files.getBytes();
        message.obj = myBytes;
        message.what = zMessageType.FILES_REMAINING;
        handler.sendMessage(message);
    }




}
