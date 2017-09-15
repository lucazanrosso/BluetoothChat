package com.lucazanrosso.bluetoothchat;

import java.io.IOException;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity {

    public final int MESSAGE_READ = 1;

    LinearLayout conversationLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        conversationLayout = (LinearLayout) findViewById(R.id.layout_conversation);
        MainActivity.mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        String readString = new String(readBuf, 0, msg.arg1);
                        View inflatedLayout = getLayoutInflater().inflate(R.layout.text_view_received,conversationLayout, false);
                        TextView textView = (TextView) inflatedLayout.findViewById(R.id.textview_receive);
                        textView.setText(readString);
                        conversationLayout.addView(inflatedLayout);
                        final ScrollView scrollview = ((ScrollView) findViewById(R.id.scrollview));
                        scrollview.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                        break;
                }
            }
        };
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.text_edit);
        String message = editText.getText().toString();
        MainActivity.mConnectedThread.write(message);
        editText.setText(null);
        View inflatedLayout = getLayoutInflater().inflate(R.layout.text_view_sent, conversationLayout, false);
        TextView textView = (TextView) inflatedLayout.findViewById(R.id.textview_send);
        textView.setText(message);
        conversationLayout.addView(inflatedLayout);
        final ScrollView scrollview = ((ScrollView) findViewById(R.id.scrollview));
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (MainActivity.mConnectedThread != null) {
            MainActivity.mConnectedThread.cancel();
            try {
                MainActivity.btSocket.close();
            } catch (IOException e2) {
                finish();
            }
        }
    }
}