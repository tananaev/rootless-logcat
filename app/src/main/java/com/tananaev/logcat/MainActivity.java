package com.tananaev.logcat;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;

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

                /*KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
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

                client.write(new BridgeMessage(
                        BridgeMessage.A_AUTH, 2, 0, new byte[256]));

                message = client.read();

                byte[] key = Hex.decodeHex("51414141414c7577666a474e363665466b667a45547347474a624c576c374351352b486852756245454b4e4f5177686b744a2f773864717630634b4c456a32524738536548664b65524d7654663867614142782f5a3742424659585935415235344a7675776d634c2f6d6e69483564705964454e43736d6b6a6238387a33586143616475444c39627062396568596869464431617368425473616158486d746147336274704a52577a6b544c554c582f6973325272444a75655a534f4a7757416b6a36666545704d7577797933596859786e76433469486b2b61667269677a7a6f426d3445703636695457566b4a323150485a6f65636d363573324749552b48414d4c5a6a4c624b47436e4e506954514376323537616b5441437475593371775970364175424542772f76587373524f6261774a5345714e574c327554334d516c354c657359764163663575754b6530674f64615773384253786b7a2f356e666f71374647486464333031686462436f7834642f7848416d6f627547726a7279524d4a532b704a5a7743564773795938394645326467564b65427a686b5462793737372b642f2f7938754362583341485338333048754e305a6772456867626252474f6868727767396a50395048457a647a2b6b556a4e7a6d6d3432336f336678327973574571486a4c7378764c5157556351434e7631466c69584b79696b36726271456b6d2b6d3435694138536a58532f47626b4d652b78426d714d53546e6b4c41684836414a6e486c7275496b7a614a356b523774747535665767553156466d473368376c76324d4e744a4867557852584f5a516658636e7156693742745869675163774775542b667766343834647a56776a68722f6334786d5a5a686a704c6f59726f574c4e4c5533363245786a61414f797451387a49396142416973734e6c2f61567a67524464704472796778637a54614145414151413d206174616e616e6165764061756b2d6d61636174616e616e61657600".toCharArray());

                client.write(new BridgeMessage(
                        BridgeMessage.A_AUTH, 3, 0, key));

                message = client.read();*/

                /*while (message.getCommand() == BridgeMessage.A_AUTH) {

                    if (!triedAuthentication) {

                        client.write(new BridgeMessage(
                                BridgeMessage.A_AUTH, 2, 0, message.getData()));

                        message = client.read();

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        stream.write(headerOID);
                        stream.write(token);
                        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                        cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey());
                        return cipher.doFinal(stream.toByteArray());

                        triedAuthentication = true;

                    } else if (!sendPublicKey) {

                        client.write(new BridgeMessage(
                                BridgeMessage.A_AUTH, 3, 0, publicKey.getEncoded()));

                        message = client.read();

                        sendPublicKey = true;

                    } else {

                        Log.w(TAG, "Authentication failed");

                    }

                }*/

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
