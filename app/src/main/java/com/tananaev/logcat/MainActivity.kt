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
@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package com.tananaev.logcat

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.tananaev.logcat.view.FilterOptionsController
import com.tananaev.logcat.view.SearchOptionsController
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

abstract class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: LineAdapter
    private lateinit var keyPair: KeyPair
    private var readerTask: ReaderTask? = null
    private var statusItem: MenuItem? = null
    private var reconnectItem: MenuItem? = null
    private var scrollItem: MenuItem? = null
    private var filterItem: MenuItem? = null
    private var searchItem: MenuItem? = null
    private var moreMenuItem: MenuItem? = null
    private var scroll = true

    private class StatusUpdate(
        val statusMessage: Int,
        val lines: List<String>?,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView = findViewById<View>(android.R.id.list) as RecyclerView
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        val animator: SimpleItemAnimator = DefaultItemAnimator()
        animator.moveDuration = ANIMATION_DURATION
        animator.addDuration = ANIMATION_DURATION
        animator.changeDuration = ANIMATION_DURATION
        animator.removeDuration = ANIMATION_DURATION
        recyclerView.itemAnimator = animator
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    updateScrollState(false)
                }
            }
        })
        adapter = LineAdapter()
        recyclerView.adapter = adapter
        try {
            keyPair = getKeyPair() // crashes on non-main thread
        } catch (e: GeneralSecurityException) {
            Log.w(TAG, e)
        } catch (e: IOException) {
            Log.w(TAG, e)
        }
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (!preferences.getBoolean(KEY_WARNING_SHOWN, false)) {
            WarningFragment().show(supportFragmentManager, null)
            preferences.edit().putBoolean(KEY_WARNING_SHOWN, true).apply()
        }
    }

    private fun updateScrollState(scroll: Boolean) {
        this.scroll = scroll
        if (scroll) {
            scrollItem!!.setIcon(R.drawable.ic_vertical_align_bottom)
            recyclerView.scrollToPosition(adapter.itemCount - 1)
        } else {
            scrollItem!!.setIcon(R.drawable.ic_vertical_align_center)
        }
    }

    private fun stopReader() {
        adapter.clear()
        if (readerTask != null) {
            readerTask!!.cancel(true)
            readerTask = null
        }
    }

    private fun restartReader() {
        stopReader()
        readerTask = ReaderTask()
        readerTask!!.execute()
    }

    private val shareIntent: Intent
        get() {
            val file = File(externalCacheDir.toString() + TEMP_FILE)
            if (file.exists()) {
                file.delete()
            }
            try {
                file.createNewFile()
                val writer = BufferedWriter(FileWriter(file))
                for (line in adapter.lines) {
                    writer.write(line.content)
                    writer.newLine()
                }
                writer.close()
            } catch (e: IOException) {
                Log.w(TAG, e)
            }
            val uri = FileProvider.getUriForFile(this, packageName, file)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            return intent
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        statusItem = menu.findItem(R.id.view_status)
        reconnectItem = menu.findItem(R.id.action_reconnect)
        scrollItem = menu.findItem(R.id.action_scroll)
        filterItem = menu.findItem(R.id.action_filter)
        searchItem = menu.findItem(R.id.action_search)
        moreMenuItem = menu.findItem(R.id.action_more)
        restartReader()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        stopReader()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reconnect -> restartReader()
            R.id.action_scroll -> updateScrollState(!scroll)
            R.id.action_share -> startActivity(
                Intent.createChooser(
                    shareIntent,
                    getString(R.string.menu_share)
                )
            )
            R.id.action_filter -> showFilterDialog()
            R.id.action_delete_all -> adapter.clear()
            R.id.action_search -> showSearchDialog()
            else -> return false
        }
        return true
    }

    private fun showFilterDialog() {
        val builder = AlertDialog.Builder(this)
        val filterOptionsViewController = FilterOptionsController(this)
        filterOptionsViewController.tag = adapter.tag
        filterOptionsViewController.keyword = adapter.keyword
        builder.setView(filterOptionsViewController.baseView)
        builder.setPositiveButton(android.R.string.ok) { dialog, which ->
            val tag = filterOptionsViewController.tag
            val keyword = filterOptionsViewController.keyword
            adapter.filter(tag, keyword)
            filterOptionsViewController.saveTagKeyword(tag, keyword)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog: Dialog = builder.create()
        dialog.window!!.setGravity(Gravity.TOP)
        dialog.show()
    }

    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(this)
        val searchOptionsViewController = SearchOptionsController(this)
        searchOptionsViewController.searchWord = adapter.searchWord
        builder.setView(searchOptionsViewController.baseView)
        builder.setPositiveButton(android.R.string.ok) { dialog, which ->
            val searchWord = searchOptionsViewController.searchWord
            adapter.search(searchWord)
            searchOptionsViewController.saveSearchWord(searchWord)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog: Dialog = builder.create()
        dialog.window!!.setGravity(Gravity.TOP)
        dialog.show()
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun getKeyPair(): KeyPair {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val keyPair: KeyPair
        if (preferences.contains(KEY_PUBLIC) && preferences.contains(KEY_PRIVATE)) {
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey = keyFactory.generatePublic(
                X509EncodedKeySpec(
                    Base64.decode(preferences.getString(KEY_PUBLIC, null), Base64.DEFAULT)
                )
            )
            val privateKey = keyFactory.generatePrivate(
                PKCS8EncodedKeySpec(
                    Base64.decode(preferences.getString(KEY_PRIVATE, null), Base64.DEFAULT)
                )
            )
            keyPair = KeyPair(publicKey, privateKey)
        } else {
            val generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(2048)
            keyPair = generator.generateKeyPair()
            preferences
                .edit()
                .putString(
                    KEY_PUBLIC,
                    Base64.encodeToString(keyPair.public.encoded, Base64.DEFAULT)
                )
                .putString(
                    KEY_PRIVATE,
                    Base64.encodeToString(keyPair.private.encoded, Base64.DEFAULT)
                )
                .apply()
        }
        return keyPair
    }

    @SuppressLint("StaticFieldLeak")
    private inner class ReaderTask : AsyncTask<Void?, StatusUpdate, Void?>() {

        override fun doInBackground(vararg params: Void?): Void? {
            val reader: Reader = RemoteReader(keyPair)
            //Reader reader = new LocalReader();
            reader.read(object : Reader.UpdateHandler {
                override val isCancelled: Boolean
                    get() = this@ReaderTask.isCancelled

                override fun update(status: Int, lines: List<String>?) {
                    publishProgress(StatusUpdate(status, lines))
                }
            })
            return null
        }

        override fun onProgressUpdate(vararg values: StatusUpdate) {
            for (statusUpdate in values) {
                if (statusUpdate.statusMessage != 0) {
                    statusItem?.setTitle(statusUpdate.statusMessage)
                    reconnectItem?.isVisible = statusUpdate.statusMessage != R.string.status_active
                    scrollItem?.isVisible = statusUpdate.statusMessage == R.string.status_active
                    filterItem?.isVisible = statusUpdate.statusMessage == R.string.status_active
                    searchItem?.isVisible = statusUpdate.statusMessage == R.string.status_active
                    moreMenuItem?.isVisible = statusUpdate.statusMessage == R.string.status_active
                }
                statusUpdate.lines?.let {
                    adapter.addItems(it)
                    if (scroll) {
                        recyclerView.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        }

    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val KEY_PUBLIC = "publicKey"
        private const val KEY_PRIVATE = "privateKey"
        private const val KEY_WARNING_SHOWN = "warningShown"
        private const val TEMP_FILE = "/logcat.txt"
        private const val ANIMATION_DURATION: Long = 75
    }

}
