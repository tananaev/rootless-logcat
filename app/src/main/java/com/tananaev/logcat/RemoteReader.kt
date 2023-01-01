/*
 * Copyright 2016 - 2022 Anton Tananaev (anton.tananaev@gmail.com)
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
package com.tananaev.logcat

import android.util.Base64
import android.util.Log
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import java.io.IOException
import java.net.Socket
import java.security.KeyPair

class RemoteReader(private val keyPair: KeyPair) : Reader {

    override fun read(updateHandler: Reader.UpdateHandler) {
        var connection: AdbConnection? = null
        try {
            updateHandler.update(R.string.status_connecting, null)
            val socket = Socket("localhost", 5555)
            val crypto = AdbCrypto.loadAdbKeyPair(
                { data -> Base64.encodeToString(data, Base64.NO_WRAP) },
                keyPair,
            )
            connection = AdbConnection.create(socket, crypto)
            connection.connect()
            updateHandler.update(R.string.status_opening, null)
            val stream = connection.open("shell:logcat -v time")
            updateHandler.update(R.string.status_active, null)
            while (!updateHandler.isCancelled) {
                val lines: MutableList<String> = ArrayList()
                for (line in String(stream.read()).split("\\r?\\n").toTypedArray()) {
                    if (line.isNotEmpty()) {
                        lines.add(line)
                    }
                }
                updateHandler.update(0, lines)
            }
        } catch (e: InterruptedException) {
            try {
                connection?.close()
            } catch (ee: IOException) {
                Log.w(TAG, ee)
            }
        } catch (e: IOException) {
            Log.w(TAG, e)
        }
    }

    companion object {
        private val TAG = RemoteReader::class.java.simpleName
    }

}
