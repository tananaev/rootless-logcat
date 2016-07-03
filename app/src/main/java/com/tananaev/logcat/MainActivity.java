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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(android.R.id.list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new LineAdapter();
        recyclerView.setAdapter(adapter);

        try {
            keyPair = getKeyPair(); // crashes on non-main thread
        } catch (GeneralSecurityException | IOException e) {
            Log.w(TAG, e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        readerTask = new ReaderTask();
        readerTask.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();

        readerTask.cancel(false);
        readerTask = null;
    }

    @SuppressWarnings("deprecation")
    private KeyPair getKeyPair() throws GeneralSecurityException, IOException {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        KeyPair keyPair;

        if (preferences.contains(KEY_PUBLIC)) {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(
                    Base64.decode(preferences.getString(KEY_PUBLIC, null), Base64.DEFAULT)));
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(
                    Base64.decode(preferences.getString(KEY_PRIVATE, null), Base64.DEFAULT)));

            keyPair = new KeyPair(publicKey, privateKey);
        } else {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            keyPair = kpg.generateKeyPair();

            preferences
                    .edit()
                    .putString(KEY_PUBLIC, Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT))
                    .putString(KEY_PRIVATE, Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT))
                    .apply();
        }

        return keyPair;
    }

    private class ReaderTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {

                Socket socket = new Socket("localhost", 5555);

                AdbCrypto crypto = AdbCrypto.loadAdbKeyPair(new AdbBase64() {
                    @Override
                    public String encodeToString(byte[] data) {
                        return Base64.encodeToString(data, Base64.DEFAULT);
                    }
                }, keyPair);

                AdbConnection connection = AdbConnection.create(socket, crypto);

                connection.connect();

                AdbStream stream = connection.open("shell:export ANDROID_LOG_TAGS=\"''\"; exec logcat");

                BufferedReader reader = new BufferedReader(new InputStreamReader(new AdbInputStream(stream)));
                while (true) {
                    publishProgress(reader.readLine());
                }

            } catch (Exception e) {
                Log.w(TAG, e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... items) {
            for (String item : items) {
                if (adapter.addItem(item)) {
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        }

    }

}
