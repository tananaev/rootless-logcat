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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.tananaev.logcat.view.FilterOptionsViewController;
import com.tananaev.logcat.view.SearchOptionsViewController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY_PUBLIC = "publicKey";
    private static final String KEY_PRIVATE = "privateKey";

    private static final String KEY_WARNING_SHOWN = "warningShown";

    private static final String TEMP_FILE = "/logcat.txt";

    private static final long ANIMATION_DURATION = 75;

    private RecyclerView recyclerView;
    private LineAdapter adapter;

    private KeyPair keyPair;
    private ReaderTask readerTask;

    private MenuItem statusItem;
    private MenuItem reconnectItem;
    private MenuItem scrollItem;
    private MenuItem filterItem;
    private MenuItem searchItem;
    private MenuItem moreMenuItem;

    private boolean scroll = true;

    private static class StatusUpdate {
        private int statusMessage;
        private List<String> lines;

        public StatusUpdate(int statusMessage, List<String> lines) {
            this.statusMessage = statusMessage;
            this.lines = lines;
        }

        public int getStatusMessage() {
            return statusMessage;
        }

        public List<String> getLines() {
            return lines;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(android.R.id.list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        SimpleItemAnimator animator = new DefaultItemAnimator();
        animator.setMoveDuration(ANIMATION_DURATION);
        animator.setAddDuration(ANIMATION_DURATION);
        animator.setChangeDuration(ANIMATION_DURATION);
        animator.setRemoveDuration(ANIMATION_DURATION);
        recyclerView.setItemAnimator(animator);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    updateScrollState(false);
                }
            }
        });

        adapter = new LineAdapter();
        recyclerView.setAdapter(adapter);

        try {
            keyPair = getKeyPair(); // crashes on non-main thread
        } catch (GeneralSecurityException | IOException e) {
            Log.w(TAG, e);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean(KEY_WARNING_SHOWN, false)) {
            new WarningFragment().show(getFragmentManager(), null);
            preferences.edit().putBoolean(KEY_WARNING_SHOWN, true).apply();
        }
    }

    private void updateScrollState(boolean scroll) {
        this.scroll = scroll;
        if (scroll) {
            scrollItem.setIcon(R.drawable.ic_vertical_align_bottom_white_24dp);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        } else {
            scrollItem.setIcon(R.drawable.ic_vertical_align_center_white_24dp);
        }
    }

    private void stopReader() {
        adapter.clear();
        if (readerTask != null) {
            readerTask.cancel(true);
            readerTask = null;
        }
    }

    private void restartReader() {
        stopReader();
        readerTask = new ReaderTask();
        readerTask.execute();
    }

    private Intent getShareIntent() {
        File file = new File(getExternalCacheDir() + TEMP_FILE);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (Line line : adapter.getLines()) {
                writer.write(line.getContent());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            Log.w(TAG, e);
        }

        Uri uri = Uri.fromFile(new File(getExternalCacheDir() + TEMP_FILE));

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        statusItem = menu.findItem(R.id.view_status);
        reconnectItem = menu.findItem(R.id.action_reconnect);
        scrollItem = menu.findItem(R.id.action_scroll);
        filterItem = menu.findItem(R.id.action_filter);
        searchItem = menu.findItem(R.id.action_search);
        moreMenuItem = menu.findItem(R.id.action_more);

        restartReader();

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopReader();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reconnect:
                restartReader();
                break;
            case R.id.action_scroll:
                updateScrollState(!scroll);
                break;
            case R.id.action_share:
                startActivity(Intent.createChooser(getShareIntent(), getString(R.string.menu_share)));
                break;
            case R.id.action_filter:
                showFilterDialog();
                break;
            case R.id.action_delete:
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.clear_log_confirm)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.clear();
                            }
                        })
                        .show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED);
                break;
            case R.id.action_search:
                showSearchDialog();
                break;
            default:
                return false;
        }
        return true;
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final FilterOptionsViewController filterOptionsViewController = new FilterOptionsViewController(this);
        filterOptionsViewController.setTag(adapter.getTag());
        filterOptionsViewController.setKeyword(adapter.getKeyword());
        builder.setView(filterOptionsViewController.getBaseView());

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tag = filterOptionsViewController.getTag();
                String keyword = filterOptionsViewController.getKeyword();
                adapter.filter(tag, keyword);
                filterOptionsViewController.saveTagKeyword(tag, keyword);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.TOP);
        dialog.show();
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final SearchOptionsViewController searchOptionsViewController = new SearchOptionsViewController(this);
        searchOptionsViewController.setSearchWord(adapter.getSearchWord());
        builder.setView(searchOptionsViewController.getBaseView());

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String searchWord = searchOptionsViewController.getSearchWord();
                adapter.search(searchWord);
                searchOptionsViewController.saveSearchWord(searchWord);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.TOP);
        dialog.show();
    }

    private KeyPair getKeyPair() throws GeneralSecurityException, IOException {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        KeyPair keyPair;

        if (preferences.contains(KEY_PUBLIC) && preferences.contains(KEY_PRIVATE)) {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(
                    Base64.decode(preferences.getString(KEY_PUBLIC, null), Base64.DEFAULT)));
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(
                    Base64.decode(preferences.getString(KEY_PRIVATE, null), Base64.DEFAULT)));

            keyPair = new KeyPair(publicKey, privateKey);
        } else {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            keyPair = generator.generateKeyPair();

            preferences
                    .edit()
                    .putString(KEY_PUBLIC, Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT))
                    .putString(KEY_PRIVATE, Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT))
                    .apply();
        }

        return keyPair;
    }

    private class ReaderTask extends AsyncTask<Void, StatusUpdate, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Reader reader = new RemoteReader(keyPair);
            //Reader reader = new LocalReader();

            reader.read(new Reader.UpdateHandler() {
                @Override
                public boolean isCancelled() {
                    return ReaderTask.this.isCancelled();
                }

                @Override
                public void update(int status, List<String> lines) {
                    publishProgress(new StatusUpdate(status, lines));
                }
            });

            return null;
        }

        @Override
        protected void onProgressUpdate(StatusUpdate... items) {
            for (StatusUpdate statusUpdate : items) {
                if (statusUpdate.getStatusMessage() != 0) {
                    statusItem.setTitle(statusUpdate.getStatusMessage());
                    reconnectItem.setVisible(statusUpdate.getStatusMessage() != R.string.status_active);
                    scrollItem.setVisible(statusUpdate.getStatusMessage() == R.string.status_active);
                    filterItem.setVisible(statusUpdate.getStatusMessage() == R.string.status_active);
                    searchItem.setVisible(statusUpdate.getStatusMessage() == R.string.status_active);
                    moreMenuItem.setVisible(statusUpdate.getStatusMessage() == R.string.status_active);
                }
                if (statusUpdate.getLines() != null) {
                    adapter.addItems(statusUpdate.getLines());
                    if (scroll) {
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }
            }
        }

    }

}
