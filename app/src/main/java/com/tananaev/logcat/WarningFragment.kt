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

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.DialogFragment

class WarningFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
            .setMessage(R.string.warning_text)
            .setPositiveButton(R.string.warning_more) { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://github.com/tananaev/rootless-logcat/blob/master/README.md")
                startActivity(intent)
            }
            .setNegativeButton(R.string.warning_close, null)
            .create()
    }

}
