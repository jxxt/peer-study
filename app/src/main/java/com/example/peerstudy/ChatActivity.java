package com.example.peerstudy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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
    private ArrayAdapter<String> adapter;
    private ArrayList<String> messageList;
    private DatabaseReference databaseReference;
    private DatabaseReference timeSpentRef; // Reference for storing time spent

    private String roomName;
    private String userFullName;
    private long startTime;  // To store when the user entered the room
    private long timeSpentInRoom; // To store total time spent

    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private int elapsedTime = 0; // Track elapsed time in seconds

    private TextView timerTextView;
    private ValueEventListener messageListener; // Global reference for message listener

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        roomName = getIntent().getStringExtra("ROOM_NAME");
        userFullName = getIntent().getStringExtra("USER_FULL_NAME");

        messageListView = findViewById(R.id.messageListView);
        messageList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
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
                    messageInput.setText(""); // Clear the input field
                } else {
                    Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Exit button logic
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer(); // Stop the timer
                saveTimeSpent(); // Save the time spent to Firebase
                finish();
//                destroyActivity(); // Completely destroy the activity and go to the room list
                Toast.makeText(ChatActivity.this, "Char Room left!", Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void startTimer() {
        startTime = System.currentTimeMillis(); // Mark the time user joined the room

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                elapsedTime++;  // Increase time by 1 second
                timerTextView.setText(elapsedTime + " seconds");
                handler.postDelayed(this, 1000); // Repeat every second
            }
        };

        handler.post(timerRunnable); // Start the timer
    }

    private void stopTimer() {
        handler.removeCallbacks(timerRunnable); // Stop updating the timer
        long endTime = System.currentTimeMillis();
        timeSpentInRoom = (endTime - startTime) / 1000; // Calculate time spent in seconds
    }

    private void saveTimeSpent() {
        timeSpentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long previousTimeSpent = 0;
                if (dataSnapshot.exists()) {
                    previousTimeSpent = dataSnapshot.getValue(Long.class); // Get the previous time spent
                }

                long updatedTimeSpent = previousTimeSpent + timeSpentInRoom; // Add current session time to previous time
                timeSpentRef.setValue(updatedTimeSpent); // Update Firebase with the new total time
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Error saving time", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void destroyActivity() {
//        // Navigate back to the study room list activity and completely destroy ChatActivity
//        Intent intent = new Intent(ChatActivity.this, WelcomeActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear task stack
//        startActivity(intent);
//        finishAffinity(); // Completely finish and remove ChatActivity from the stack
//    }

    private void loadMessages() {
        messageListener = new ValueEventListener() {
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
        };

        databaseReference.addValueEventListener(messageListener); // Attach the listener
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

    // Override onBackPressed to do nothing, preventing the back button from exiting the room
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please first exit the room!", Toast.LENGTH_SHORT).show();
        // Do not call super.onBackPressed() to prevent default behavior.
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            // This allows you to detect if the app is finishing naturally.
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
