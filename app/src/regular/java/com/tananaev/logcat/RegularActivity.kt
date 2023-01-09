package com.tananaev.logcat

import android.os.Bundle

class RegularActivity : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)
    }
}
