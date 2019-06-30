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

import android.util.Base64;
import android.util.Log;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

public class RemoteReader implements Reader {

    private static final String TAG = RemoteReader.class.getSimpleName();

    private KeyPair keyPair;

    public RemoteReader(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    @Override
    public void read(UpdateHandler updateHandler) {

        AdbConnection connection = null;

        try {

            updateHandler.update(R.string.status_connecting, null);

            Socket socket = new Socket("localhost", 5555);

            AdbCrypto crypto = AdbCrypto.loadAdbKeyPair(new AdbBase64() {
                @Override
                public String encodeToString(byte[] data) {
                    return Base64.encodeToString(data, Base64.NO_WRAP);
                }
            }, keyPair);

            connection = AdbConnection.create(socket, crypto);

            connection.connect();

            updateHandler.update(R.string.status_opening, null);

            AdbStream stream = connection.open("shell:logcat -v time");

            updateHandler.update(R.string.status_active, null);

            while (!updateHandler.isCancelled()) {
                List<String> lines = new ArrayList<>();
                for (String line : new String(stream.read()).split("\\r?\\n")) {
                    if (!line.isEmpty()) {
                        lines.add(line);
                    }
                }
                updateHandler.update(0, lines);
            }

        } catch (InterruptedException e) {
            try {
                connection.close();
            } catch (IOException ee) {
                Log.w(TAG, ee);
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        }

    }

}
