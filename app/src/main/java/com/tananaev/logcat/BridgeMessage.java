package com.tananaev.logcat;

public class BridgeMessage {

    public static int A_SYNC = 0x434e5953;
    public static int A_CNXN = 0x4e584e43;
    public static int A_AUTH = 0x48545541;
    public static int A_OPEN = 0x4e45504f;
    public static int A_OKAY = 0x59414b4f;
    public static int A_CLSE = 0x45534c43;
    public static int A_WRTE = 0x45545257;

    private int command;
    private int arg0;
    private int arg1;
    private byte[] data;

    public BridgeMessage(int command, int arg0, int arg1, byte[] data) {
        this.command = command;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.data = data;
    }

    public int getCommand() {
        return command;
    }

    public int getArg0() {
        return arg0;
    }

    public int getArg1() {
        return arg1;
    }

    public byte[] getData() {
        return data;
    }

}
