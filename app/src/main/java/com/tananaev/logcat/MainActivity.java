package com.tananaev.logcat;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.zip.CRC32;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private LineAdapter adapter;

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

    private class ReaderTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {

                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(512);
                KeyPair kp = kpg.genKeyPair();
                Key publicKey = kp.getPublic();
                Key privateKey = kp.getPrivate();

                boolean triedAuthentication = false;
                boolean sendPublicKey = false;

                BridgeClient client = new BridgeClient("localhost", 5555);

                client.write(new BridgeMessage(
                        BridgeMessage.A_CNXN, 0x01000000, 0x00040000, "host::features=cmd,shell_v2".getBytes()));

                BridgeMessage message = client.read();

                while (message.getCommand() == BridgeMessage.A_AUTH) {

                    if (!triedAuthentication) {

                        client.write(new BridgeMessage(
                                BridgeMessage.A_AUTH, 2, 0, message.getData()));

                        message = client.read();

                        /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        stream.write(headerOID);
                        stream.write(token);
                        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                        cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey());
                        return cipher.doFinal(stream.toByteArray());*/

                        triedAuthentication = true;

                    } else if (!sendPublicKey) {

                        client.write(new BridgeMessage(
                                BridgeMessage.A_AUTH, 3, 0, publicKey.getEncoded()));

                        message = client.read();

                        sendPublicKey = true;

                    } else {

                        Log.w(TAG, "Authentication failed");

                    }

                }

                publishProgress("lol");

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

    };

}
