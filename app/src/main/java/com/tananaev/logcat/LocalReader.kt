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

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class LocalReader : Reader {

    override fun read(updateHandler: Reader.UpdateHandler) {
        try {
            val command = arrayOf("logcat", "-v", "time")
            updateHandler.update(R.string.status_opening, null)
            val process = Runtime.getRuntime().exec(command)
            val bufferedReader = BufferedReader(
                InputStreamReader(process.inputStream)
            )
            updateHandler.update(R.string.status_active, null)
            while (!updateHandler.isCancelled) {
                updateHandler.update(0, listOf(bufferedReader.readLine()))
            }
        } catch (e: IOException) {
            Log.w(TAG, e)
        }
    }

    companion object {
        private val TAG = RemoteReader::class.java.simpleName
    }

}
