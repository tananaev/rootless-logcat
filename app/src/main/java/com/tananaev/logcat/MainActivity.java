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

        private int A_SYNC = 0x434e5953;
        private int A_CNXN = 0x4e584e43;
        private int A_AUTH = 0x48545541;
        private int A_OPEN = 0x4e45504f;
        private int A_OKAY = 0x59414b4f;
        private int A_CLSE = 0x45534c43;
        private int A_WRTE = 0x45545257;

        /*
        unsigned command;       // command identifier constant      /
        unsigned arg0;          // first argument                   /
        unsigned arg1;          // second argument                  /
        unsigned data_length;   // length of payload (0 is allowed) /
        unsigned data_crc32;    // crc32 of data payload            /
        unsigned magic;         // command ^ 0xffffffff             /
        */

        public class Message {

            private int command;
            private int arg0;
            private int arg1;
            private String data;

            public Message(int command, int arg0, int arg1, String data) {
                this.command = command;
                this.arg0 = arg0;
                this.arg1 = arg1;
                this.data = data;
            }

            public int getCommand() {
                return command;
            }

            public int getArg0() {
                return arg0;
            }

            public int getArg1() {
                return arg1;
            }

            public String getData() {
                return data;
            }

        }


        private int sum(String data) {
            byte[] bytes = data.getBytes();
            long result = 0;
            int count = bytes.length;
            while (count-- > 0) {
                result += ((long) bytes[count]) & 0xff;
            }
            return (int) result;
        }

        private void write(OutputStream stream, Message message) throws IOException {

            ByteBuffer buffer = ByteBuffer.allocate(6 * 4 + message.getData().length());
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            buffer.putInt(message.getCommand());
            buffer.putInt(message.getArg0());
            buffer.putInt(message.getArg1());
            buffer.putInt(message.getData().length());

            buffer.putInt(sum(message.getData()));

            buffer.putInt(~message.getCommand());
            buffer.put(message.getData().getBytes());

            buffer.flip();

            WritableByteChannel channel = Channels.newChannel(stream);
            channel.write(buffer);

        }

        private Message read(InputStream stream) throws IOException {

            ReadableByteChannel channel = Channels.newChannel(stream);

            ByteBuffer header = ByteBuffer.allocate(6 * 4);
            header.order(ByteOrder.LITTLE_ENDIAN);

            channel.read(header);

            header.flip();

            int command = header.getInt();
            int arg0 = header.getInt();
            int arg1 = header.getInt();
            int data_length = header.getInt();
            int data_crc32 = header.getInt();
            int magic = header.getInt();

            byte[] data = new byte[data_length];
            stream.read(data);

            return new Message(command, arg0, arg1, new String(data));


            /*DataInputStream is = new DataInputStream(stream);

            int command = is.readInt();
            int arg0 = is.readInt();
            int arg1 = is.readInt();
            int data_length = is.readInt();
            int data_crc32 = is.readInt();
            int magic = is.readInt();

            byte[] data = new byte[data_length];
            is.readFully(data);

            return new Message(command, arg0, arg1, new String(data));*/

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                //Socket socket = new Socket("10.0.2.2", 5037);
                Socket socket = new Socket("localhost", 5555);

                publishProgress("");

                OutputStream os = socket.getOutputStream();
                InputStream is = socket.getInputStream();

                write(os, new Message(A_CNXN, 0x01000000, 0x00040000, "host::features=cmd,shell_v2"));

                Message message = read(is);

                publishProgress("");



                /*PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                write(out, "host:transport-any");
                in.read(new char[12]);
                write(out, "shell:logcat");

                while (!isCancelled()) {
                    publishProgress(in.readLine());
                }*/

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
