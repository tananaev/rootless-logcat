package com.tananaev.logcat;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY_PUBLIC = "publicKey";
    private static final String KEY_PRIVATE = "privateKey";

    private RecyclerView recyclerView;
    private LineAdapter adapter;

    private KeyPair keyPair;
    private ReaderTask readerTask;

    private MenuItem statusItem;
    private MenuItem reconnectItem;
    private MenuItem scrollItem;

    private boolean scroll = true;

    private static class StatusUpdate {
        private int statusMessage;
        private String line;

        public StatusUpdate(int statusMessage, String line) {
            this.statusMessage = statusMessage;
            this.line = line;
        }

        public int getStatusMessage() {
            return statusMessage;
        }

        public String getLine() {
            return line;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(android.R.id.list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        statusItem = menu.findItem(R.id.view_status);
        reconnectItem = menu.findItem(R.id.action_reconnect);
        scrollItem = menu.findItem(R.id.action_scroll);

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
        if (item.getItemId() == R.id.action_reconnect) {
            restartReader();
            return true;
        } else if (item.getItemId() == R.id.action_scroll) {
            updateScrollState(!scroll);
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            return true;
        }
        return false;
    }

    private KeyPair getKeyPair() throws GeneralSecurityException, IOException {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        KeyPair keyPair;

        if (preferences.contains(KEY_PUBLIC)) {
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

            AdbConnection connection = null;

            try {

                publishProgress(new StatusUpdate(R.string.status_connecting, null));

                Socket socket = new Socket("localhost", 5555);

                AdbCrypto crypto = AdbCrypto.loadAdbKeyPair(new AdbBase64() {
                    @Override
                    public String encodeToString(byte[] data) {
                        return Base64.encodeToString(data, Base64.DEFAULT);
                    }
                }, keyPair);

                connection = AdbConnection.create(socket, crypto);

                connection.connect();

                publishProgress(new StatusUpdate(R.string.status_opening, null));

                AdbStream stream = connection.open("shell:export ANDROID_LOG_TAGS=\"''\"; exec logcat");

                publishProgress(new StatusUpdate(R.string.status_active, null));

                BufferedReader reader = new BufferedReader(new InputStreamReader(new AdbInputStream(stream)));
                while (!isCancelled()) {
                    publishProgress(new StatusUpdate(0, reader.readLine()));
                }

            } catch (InterruptedException e) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (IOException ee) {
                        Log.w(TAG, ee);
                    }
                }
            } catch (IOException e) {
                Log.w(TAG, e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(StatusUpdate... items) {
            for (StatusUpdate statusUpdate : items) {
                if (statusUpdate.getStatusMessage() != 0) {
                    statusItem.setTitle(statusUpdate.getStatusMessage());
                    reconnectItem.setVisible(statusUpdate.getStatusMessage() != R.string.status_active);
                    scrollItem.setVisible(statusUpdate.getStatusMessage() == R.string.status_active);
                }
                if (statusUpdate.getLine() != null) {
                    if (adapter.addItem(statusUpdate.getLine()) && scroll) {
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }
            }
        }

    }

}
