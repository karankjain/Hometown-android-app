package com.example.karanjain.weconnect;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ChatHistoryActivity extends AppCompatActivity {
    
    private FirebaseListAdapter<String> adapter;
    private String activeUser;
    TextView chatUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        if ( null != FirebaseAuth.getInstance().getCurrentUser()) {
            activeUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            displayChats(activeUser);
        }
    }

    protected void displayChats(String uid) {
        ListView list = (ListView) findViewById(R.id.listView1);

        adapter = new FirebaseListAdapter<String>(this, String.class,
                R.layout.chat_history_row, FirebaseDatabase.getInstance().getReference().child("chat_users").child(uid)) {
            @Override
            protected void populateView(View v, String model, int position) {

                chatUser = (TextView) v.findViewById(R.id.chat_nickName);
                Button chatButton = (Button) v.findViewById(R.id.chat_button_history);
                chatUser.setText(model.toString());
                chatButton.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Bundle data = new Bundle();
                        View row = (View)v.getParent();
                        chatUser = (TextView) row.findViewById(R.id.chat_nickName);
                        String name = chatUser.getText().toString();
                        data.putString("nickname", name);
                        Intent intent = new Intent(ChatHistoryActivity.this,ChatActivity.class);
                        intent.putExtras(data);
                        startActivity(intent);
                    }
                });
            }
        };

        list.setAdapter(adapter);
    }
}
