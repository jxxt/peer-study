package com.example.peerstudy;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
    private MessageAdapter adapter;
    private ArrayList<Message> messageList;
    private DatabaseReference databaseReference;
    private DatabaseReference timeSpentRef;

    private String roomName;
    private String userFullName;
    private long startTime;
    private long timeSpentInRoom;

    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private int elapsedTime = 0;

    private TextView timerTextView;
    private ValueEventListener messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        roomName = getIntent().getStringExtra("ROOM_NAME");
        userFullName = getIntent().getStringExtra("USER_FULL_NAME");

        messageListView = findViewById(R.id.messageListView);
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList);
        messageListView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("chatrooms").child(roomName).child("chats");
        timeSpentRef = FirebaseDatabase.getInstance().getReference("timeSpent").child(userFullName).child(roomName);

        timerTextView = findViewById(R.id.timer);

        // Load existing messages
        loadMessages();

        // Start the timer
        startTimer();

        // Button and EditText for sending messages
        EditText messageInput = findViewById(R.id.messageInput);
        Button sendButton = findViewById(R.id.sendButton);
        Button exitButton = findViewById(R.id.exitRoom);

        TextView roomNameTV = findViewById(R.id.roomname);
        roomNameTV.setText(": " + roomName + " :");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageInput.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Exit button logic
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                saveTimeSpent();
                finish();
                Toast.makeText(ChatActivity.this, "Chat Room left!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                elapsedTime++;
                timerTextView.setText(elapsedTime + " seconds");
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(timerRunnable); // Start the timer
    }

    private void stopTimer() {
        handler.removeCallbacks(timerRunnable);
        long endTime = System.currentTimeMillis();
        timeSpentInRoom = (endTime - startTime) / 1000;
    }

    private void saveTimeSpent() {
        timeSpentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long previousTimeSpent = 0;
                if (dataSnapshot.exists()) {
                    previousTimeSpent = dataSnapshot.getValue(Long.class);
                }

                long updatedTimeSpent = previousTimeSpent + timeSpentInRoom;
                timeSpentRef.setValue(updatedTimeSpent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Error saving time", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();

                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String sender = messageSnapshot.child("sender").getValue(String.class);
                    String message = messageSnapshot.child("message").getValue(String.class);
                    long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);
                    messageList.add(new Message(sender, message, timestamp));
                }

                Collections.sort(messageList, new Comparator<Message>() {
                    @Override
                    public int compare(Message m1, Message m2) {
                        return Long.compare(m1.timestamp, m2.timestamp);
                    }
                });

                adapter.notifyDataSetChanged();
                messageListView.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        };

        databaseReference.addValueEventListener(messageListener);
    }

    private void sendMessage(String message) {
        long timestamp = System.currentTimeMillis();
        String messageId = databaseReference.push().getKey();

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("sender", userFullName);
        messageData.put("message", message);
        messageData.put("timestamp", timestamp);

        // Save message to Firebase
        if (messageId != null) {
            databaseReference.child(messageId).setValue(messageData);
        }
    }

    // Override onBackPressed to do nothing, preventing the back button from exiting the room
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Toast.makeText(this, "Please first exit the room!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            return;
        }
        // Show a Toast when the user tries to leave using the Home button.
        Toast.makeText(this, "Please first exit the room!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            databaseReference.removeEventListener(messageListener); // Remove the listener when activity is destroyed
        }
    }

    // Custom adapter to set font style and color for messages
    private class MessageAdapter extends ArrayAdapter<Message> {

        MessageAdapter(ChatActivity context, ArrayList<Message> messages) {
            super(context, 0, messages);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            Message message = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_chat_messages, parent, false);
            }

            // Lookup view for data population
            TextView messageTextView = convertView.findViewById(R.id.messageTextView);

            // Populate the data into the template view using the data object
            messageTextView.setText(message.sender + ": " + message.message);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                messageTextView.setTypeface(getResources().getFont(R.font.nova_round)); // Set custom font
            }
            messageTextView.setTextColor(getResources().getColor(R.color.font)); // Set custom text color

            // Return the completed view to render on screen
            return convertView;
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
