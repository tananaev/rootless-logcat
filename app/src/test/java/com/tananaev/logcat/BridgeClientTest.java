package com.tananaev.logcat;

import android.location.Location;
import android.util.Log;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class BridgeClientTest {

    @Test
    public void testFormatRequest() throws Exception {

        try {

            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(512);
            KeyPair kp = kpg.genKeyPair();
            Key publicKey = kp.getPublic();
            Key privateKey = kp.getPrivate();

            boolean triedAuthentication = false;
            boolean sendPublicKey = false;

            BridgeClient client = new BridgeClient("10.28.254.130", 5555);

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

                    Assert.fail("Authentication failed");

                }

            }

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

    }

}
