package com.bt.chat.v32;
import android.app.Activity;
import android.bluetooth.*;
import android.os.*;
import android.widget.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity {
    private final UUID UUID_BT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bA;
    private BluetoothSocket bS;
    private TextView log;
    private EditText input;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(40,40,40,40);
        log = new TextView(this);
        log.setText("V32 SELF-GEN ENGINE\n");
        l.addView(log);
        Button scn = new Button(this);
        scn.setText("RECHERCHER CAROLE");
        scn.setOnClickListener(v -> connect());
        l.addView(scn);
        input = new EditText(this);
        l.addView(input);
        Button snd = new Button(this);
        snd.setText("ENVOYER");
        snd.setOnClickListener(v -> send());
        l.addView(snd);
        setContentView(l);
        bA = BluetoothAdapter.getDefaultAdapter();
        if(Build.VERSION.SDK_INT >= 31) {
            requestPermissions(new String[]{"android.permission.BLUETOOTH_SCAN","android.permission.BLUETOOTH_CONNECT","android.permission.ACCESS_FINE_LOCATION"}, 1);
        }
        listen();
    }

    private void listen() {
        new Thread(() -> {
            try {
                BluetoothServerSocket s = bA.listenUsingInsecureRfcommWithServiceRecord("BT", UUID_BT);
                bS = s.accept();
                manage();
            } catch (Exception e) {}
        }).start();
    }

    private void connect() {
        Set<BluetoothDevice> paired = bA.getBondedDevices();
        for(BluetoothDevice d : paired) {
            new Thread(() -> {
                try {
                    bS = d.createInsecureRfcommSocketToServiceRecord(UUID_BT);
                    bS.connect();
                    manage();
                } catch (Exception e) {}
            }).start();
        }
    }

    private void manage() {
        runOnUiThread(() -> log.append("CONNECTÉ !\n"));
        try {
            InputStream is = bS.getInputStream();
            byte[] buf = new byte[1024];
            while(true) {
                int len = is.read(buf);
                if(len > 0) {
                    String m = new String(buf, 0, len);
                    runOnUiThread(() -> log.append("Carole: " + m + "\n"));
                }
            }
        } catch (Exception e) {}
    }

    private void send() {
        try {
            String m = input.getText().toString();
            bS.getOutputStream().write(m.getBytes());
            log.append("Moi: " + m + "\n");
            input.setText("");
        } catch (Exception e) {}
    }
}