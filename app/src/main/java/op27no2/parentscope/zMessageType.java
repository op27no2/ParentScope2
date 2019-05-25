package op27no2.parentscope;

public class zMessageType {
    public static final int DATA_SENT_OK = 0x00;
    public static final int READY_FOR_DATA = 0x01;
    public static final int DATA_RECEIVED = 0x02;
    public static final int DATA_PROGRESS_UPDATE = 0x03;
    public static final int SENDING_DATA = 0x04;
    public static final int RECEIVED_CONNECTION = 0x05;
    public static final int REQUEST = 0x06;
    public static final int PHOTO = 0x07;
    public static final int SENDER_STARTED = 0x08;
    public static final int RECEIVING_DATA = 0x09;
    public static final int FILES_REMAINING = 0x10;



    public static final int DIGEST_DID_NOT_MATCH = 0x50;
    public static final int COULD_NOT_CONNECT = 0x51;
    public static final int INVALID_HEADER = 0x52;
}
