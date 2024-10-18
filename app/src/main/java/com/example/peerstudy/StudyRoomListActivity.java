package com.example.peerstudy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class StudyRoomListActivity extends AppCompatActivity {

    private ListView roomListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> roomList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_room_list); // Set the layout

        roomListView = findViewById(R.id.roomListView);
        roomList = new ArrayList<>();

        // Use the custom list item layout
        adapter = new ArrayAdapter<>(this, R.layout.custom_list_item, R.id.roomText, roomList);
        roomListView.setAdapter(adapter);

        // Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("chatrooms");
        fetchRooms();

        // Get the user's full name from the Intent
        String userFullName = getIntent().getStringExtra("USER_FULL_NAME");

        // Handle clicking on room list items
        roomListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRoom = roomList.get(position);
            Intent intent = new Intent(StudyRoomListActivity.this, ChatActivity.class);
            intent.putExtra("ROOM_NAME", selectedRoom);
            intent.putExtra("USER_FULL_NAME", userFullName); // Pass the full name
            startActivity(intent);
        });
    }

    // Fetch room names from Firebase
    private void fetchRooms() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomList.clear(); // Clear previous data
                for (DataSnapshot roomSnapshot : dataSnapshot.getChildren()) {
                    String roomName = roomSnapshot.getKey(); // Get room name
                    roomList.add(roomName); // Add room name to the list
                }
                adapter.notifyDataSetChanged(); // Notify adapter for data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });
    }
}
