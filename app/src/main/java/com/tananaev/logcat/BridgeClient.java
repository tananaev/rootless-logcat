package com.tananaev.logcat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class BridgeClient {

    private InputStream inputStream;
    private OutputStream outputStream;

    public BridgeClient(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
    }

    private int checksum(byte[] data) {
        long result = 0;
        int count = data.length;
        while (count-- > 0) {
            result += ((long) data[count]) & 0xff;
        }
        return (int) result;
    }

    public void write(BridgeMessage message) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(6 * 4 + message.getData().length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(message.getCommand());
        buffer.putInt(message.getArg0());
        buffer.putInt(message.getArg1());
        buffer.putInt(message.getData().length);

        buffer.putInt(checksum(message.getData()));

        buffer.putInt(~message.getCommand());
        buffer.put(message.getData());

        buffer.flip();

        WritableByteChannel channel = Channels.newChannel(outputStream);
        channel.write(buffer);

    }

    public BridgeMessage read() throws IOException {

        ReadableByteChannel channel = Channels.newChannel(inputStream);

        ByteBuffer header = ByteBuffer.allocate(6 * 4);
        header.order(ByteOrder.LITTLE_ENDIAN);

        channel.read(header);

        header.flip();

        int command = header.getInt();
        int arg0 = header.getInt();
        int arg1 = header.getInt();
        int data_length = header.getInt();
        int data_crc32 = header.getInt();
        int magic = header.getInt();

        byte[] data = new byte[data_length];
        inputStream.read(data);

        return new BridgeMessage(command, arg0, arg1, data);

    }


}
