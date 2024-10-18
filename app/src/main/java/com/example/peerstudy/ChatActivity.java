package com.example.peerstudy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private ListView messageListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> messageList;
    private DatabaseReference databaseReference;
    private String roomName;
    private String userFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat); // Set your layout file here

        roomName = getIntent().getStringExtra("ROOM_NAME");
        userFullName = getIntent().getStringExtra("USER_FULL_NAME");

        messageListView = findViewById(R.id.messageListView);
        messageList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
        messageListView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("chatrooms").child(roomName).child("chats");

        // Load existing messages
        loadMessages();

        // Button and EditText for sending messages
        EditText messageInput = findViewById(R.id.messageInput);
        Button sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageInput.setText(""); // Clear the input field
                } else {
                    Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMessages() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear(); // Clear the list before adding new messages
                ArrayList<Message> messages = new ArrayList<>(); // Temporary list to store messages

                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String sender = messageSnapshot.child("sender").getValue(String.class);
                    String message = messageSnapshot.child("message").getValue(String.class);
                    long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);
                    messages.add(new Message(sender, message, timestamp)); // Add message to the temporary list
                }

                // Sort messages by timestamp in ascending order
                Collections.sort(messages, new Comparator<Message>() {
                    @Override
                    public int compare(Message m1, Message m2) {
                        return Long.compare(m1.timestamp, m2.timestamp);
                    }
                });

                // Create display messages without formatted time
                for (Message msg : messages) {
                    String displayMessage = msg.sender + ": " + msg.message; // Remove timestamp from the display message
                    messageList.add(displayMessage);
                }

                adapter.notifyDataSetChanged(); // Notify the adapter about data changes
                messageListView.setSelection(adapter.getCount() - 1); // Scroll to the last message
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String message) {
        long timestamp = System.currentTimeMillis(); // Get current timestamp
        String messageId = databaseReference.push().getKey(); // Create a unique key for the message

        // Create a message object with sender, message, and timestamp
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("sender", userFullName); // Use user's full name
        messageData.put("message", message);
        messageData.put("timestamp", timestamp); // Save the current timestamp

        // Save message to Firebase
        if (messageId != null) {
            databaseReference.child(messageId).setValue(messageData);
        }
    }

    // Message class to hold message data
    private static class Message {
        String sender;
        String message;
        long timestamp;

        Message(String sender, String message, long timestamp) {
            this.sender = sender;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}
