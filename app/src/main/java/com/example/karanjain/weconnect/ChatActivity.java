package com.example.karanjain.weconnect;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ChatActivity extends AppCompatActivity {

    private FirebaseListAdapter<Chat> adapter;
    private String nickname, activeUser, chatKey;
    private boolean doesChatExist;
    DatabaseReference myDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doesChatExist = false;
        setContentView(R.layout.activity_chat_messenger);
        Bundle data = this.getIntent().getExtras();
        nickname = data.getString("nickname");
        activeUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myDatabase = FirebaseDatabase.getInstance().getReference().child("chat_users");
        Query query = myDatabase.child(activeUser).orderByValue().equalTo(nickname);
        ValueEventListener ve = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (child.getValue().equals(nickname)) {
                        chatKey = child.getKey();
                        displayChats(chatKey);
                        doesChatExist = true;
                    }
                }

                if (!doesChatExist) {
                    chatKey = myDatabase.child(activeUser).push().getKey();
                    myDatabase.child(activeUser).child(chatKey).setValue(nickname);
                    myDatabase.child(nickname).child(chatKey).setValue(activeUser);
                    displayChats(chatKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        query.addValueEventListener(ve);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.input);

                FirebaseDatabase.getInstance()
                        .getReference().child("chats").child(chatKey).push()
                        .setValue(new Chat(input.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser().getEmail())
                        );
                input.setText("");
            }
        });
    }

    protected void displayChats(String messageId) {
        ListView list = (ListView) findViewById(R.id.list_of_messages);
        adapter = new FirebaseListAdapter<Chat>(this, Chat.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference().child("chats").child(messageId)) {
            @Override
            protected void populateView(View v, Chat model, int position) {
                TextView chatText = (TextView) v.findViewById(R.id.message_text);
                TextView chatUser = (TextView) v.findViewById(R.id.message_user);
                TextView chatTime = (TextView) v.findViewById(R.id.message_time);
                chatText.setText(model.getChatText());
                chatUser.setText(model.getChatUser());
                chatTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getChatTime()));
            }
        };
        list.setAdapter(adapter);
    }
}

