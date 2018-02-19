package com.lucazanrosso.bluetoothchat;

import java.io.IOException;
import java.util.ArrayList;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {

    public final int MESSAGE_READ = 1;

    LinearLayout conversationLayout;
    ScrollView scrollview;

    ArrayList<Message> chat = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
//        scrollview = findViewById(R.id.scrollview);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ChatAdapter(chat);
        mRecyclerView.setAdapter(mAdapter);

//        conversationLayout = findViewById(R.id.layout_conversation);
        MainActivity.mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        String readString = new String(readBuf, 0, msg.arg1);
//                        View inflatedLayout = getLayoutInflater().inflate(R.layout.text_view_received,conversationLayout, false);
//                        TextView textView = inflatedLayout.findViewById(R.id.textview_received);
//                        textView.setText(readString);
//                        conversationLayout.addView(inflatedLayout);
//
//                        scrollview.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
//                            }
//                        });

                        Toast.makeText(getApplicationContext(), readString, Toast.LENGTH_SHORT).show();
                        chat.add(new Message(readString, 1));
//                        mAdapter.notifyItemInserted(chat.size() - 1);
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.scrollToPosition(chat.size() - 1);

                        break;
                }
            }
        };
    }

    public void sendMessage(View view) {
        EditText editText = findViewById(R.id.text_edit);
        String message = editText.getText().toString();
        MainActivity.mConnectedThread.write(message);
        editText.setText(null);
//        View inflatedLayout = getLayoutInflater().inflate(R.layout.text_view_sent, conversationLayout, false);
//        TextView textView = inflatedLayout.findViewById(R.id.textview_sent);
//        textView.setText(message);
//        conversationLayout.addView(inflatedLayout);
//        final ScrollView scrollview = findViewById(R.id.scrollview);
//        scrollview.post(new Runnable() {
//            @Override
//            public void run() {
//                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
//            }
//        });

        chat.add(new Message(message, 0));
//        mAdapter.notifyItemInserted(chat.size() - 1);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(chat.size() - 1);

    }

    public void scrollDown(View view) {
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                scrollview.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        scrollview.fullScroll(ScrollView.FOCUS_DOWN);
//                    }
//                });
//            }
//        }, 200);
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