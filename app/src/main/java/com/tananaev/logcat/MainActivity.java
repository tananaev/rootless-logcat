package com.tananaev.logcat;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

        private void write(PrintWriter out, String command) {
            out.printf("%04x%s", command.length(), command);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                //Socket socket = new Socket("10.0.2.2", 5037);
                Socket socket = new Socket("localhost", 5555);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                write(out, "host:transport-any");
                in.read(new char[12]);
                write(out, "shell:logcat");

                while (!isCancelled()) {
                    publishProgress(in.readLine());
                }

            } catch (IOException e) {
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
