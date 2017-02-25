package com.lucazanrosso.bluetoothchat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.support.v7.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "SI";
    public final int MESSAGE_READ = 1;

    Handler mHandler;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private ConnectedThread mConnectedThread;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "98:D3:32:20:68:87";

    LinearLayout conversationLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        conversationLayout = (LinearLayout) findViewById(R.id.layout_conversation);

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        String readString = new String(readBuf, 0, msg.arg1);
                        View inflatedLayout = getLayoutInflater().inflate(R.layout.text_view_received, null, false);
                        TextView textView = (TextView) inflatedLayout.findViewById(R.id.textview_receive);
                        textView.setText(readString);
                        conversationLayout.addView(inflatedLayout);
                        break;
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
//        checkBTState();
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.text_edit);
        String message = editText.getText().toString();
        mConnectedThread.write(message);
        editText.setText(null);
        View inflatedLayout = getLayoutInflater().inflate(R.layout.text_view_sent, null, false);
        TextView textView = (TextView) inflatedLayout.findViewById(R.id.textview_send);
        textView.setText(message);
        conversationLayout.addView(inflatedLayout);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    if (mmInStream.available() > 0) {
                        // Read from the InputStream.
                        numBytes = mmInStream.read(mmBuffer);
                        // Send the obtained bytes to the UI activity.
                        mHandler.obtainMessage(MESSAGE_READ, numBytes, -1, mmBuffer)
                                .sendToTarget();
                    } else SystemClock.sleep(200);
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(String message) {
            try {
                byte[] bytes = message.getBytes();
                mmOutStream.write(bytes);
                // Share the sent message with the UI activity.
//                    mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, mmBuffer)
//                            .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Fatal Error" + " - " + "In onResume() and socket create failed: " + e.getMessage() + ".", Toast.LENGTH_LONG).show();
            finish();
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "Fatal Error" + " - " + "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }
}