/*
 * Copyright 2016 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
