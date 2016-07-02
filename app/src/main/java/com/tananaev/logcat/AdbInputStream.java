package com.tananaev.logcat;

import com.tananaev.adblib.AdbStream;

import java.io.IOException;
import java.io.InputStream;

public class AdbInputStream extends InputStream {

    private AdbStream stream;

    private int index;
    private byte[] buffer;

    public AdbInputStream(AdbStream stream) {
        this.stream = stream;
    }

    @Override
    public int available() throws IOException {
        if (buffer != null) {
            return buffer.length - index;
        }
        return 0;
    }

    @Override
    public int read() throws IOException {
        try {
            while (buffer == null || index >= buffer.length) {
                index = 0;
                buffer = stream.read();
            }
            return buffer[index++];
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            while (buffer == null || index >= buffer.length) {
                index = 0;
                buffer = stream.read();
            }
            int count = Math.min(len, buffer.length - index);
            System.arraycopy(buffer, index, b, off, count);
            index += count;
            return count;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

}
