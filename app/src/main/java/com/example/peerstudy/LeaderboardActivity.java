package com.example.peerstudy;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    private Spinner roomSpinner;
    private LinearLayout leaderboardLayout;
    private DatabaseReference dbRef;
    private String userFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        roomSpinner = findViewById(R.id.roomSpinner);
        leaderboardLayout = findViewById(R.id.leaderboardLayout);
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Fetch user name passed from WelcomeActivity
        userFullName = getIntent().getStringExtra("USER_FULL_NAME");

        // Fetch room names from Firebase
        fetchRoomNames();
    }

    private void fetchRoomNames() {
        dbRef.child("chatrooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> rooms = new ArrayList<>();
                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    rooms.add(roomSnapshot.getKey()); // Add room names to the list
                }

                // Populate the spinner with room names
                ArrayAdapter<String> adapter = new ArrayAdapter<>(LeaderboardActivity.this, android.R.layout.simple_spinner_item, rooms);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                roomSpinner.setAdapter(adapter);

                // Set up listener for room selection
                roomSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        String selectedRoom = rooms.get(position);
                        updateLeaderboard(selectedRoom);
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
            }
        });
    }

    private void updateLeaderboard(String room) {
        leaderboardLayout.removeAllViews();

        dbRef.child("timeSpent").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> userTimeMap = new HashMap<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userName = userSnapshot.getKey();
                    Integer timeSpent = userSnapshot.child(room).getValue(Integer.class);
                    if (timeSpent != null) {
                        userTimeMap.put(userName, timeSpent);
                    }
                }

                // Sort users by time spent (descending order)
                ArrayList<Map.Entry<String, Integer>> userList = new ArrayList<>(userTimeMap.entrySet());
                Collections.sort(userList, (u1, u2) -> u2.getValue().compareTo(u1.getValue()));

                // Set up the leaderboard
                if (!userList.isEmpty()) {
                    int maxTime = userList.get(0).getValue();  // Maximum time spent for full progress bar

                    for (Map.Entry<String, Integer> user : userList) {
                        String userName = user.getKey();
                        int timeSpent = user.getValue();

                        // Create TextView for each user
                        TextView userTextView = new TextView(LeaderboardActivity.this);
                        userTextView.setText(userName + ": " + timeSpent + " mins");
                        userTextView.setTextSize(16);
                        leaderboardLayout.addView(userTextView);

                        // Create ProgressBar for each user
                        ProgressBar progressBar = new ProgressBar(LeaderboardActivity.this, null, android.R.attr.progressBarStyleHorizontal);
                        progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50));
                        progressBar.setMax(maxTime);
                        progressBar.setProgress(timeSpent);

                        leaderboardLayout.addView(progressBar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
            }
        });
    }
}
