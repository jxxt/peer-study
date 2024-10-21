package com.example.peerstudy;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudyRoomListActivity extends AppCompatActivity {

    private static final String TAG = "StudyRoomListActivity";
    private ListView roomListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> roomList;
    private DatabaseReference databaseReference;
    private String userFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_room_list);

        roomListView = findViewById(R.id.roomListView);
        roomList = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, R.layout.custom_list_item, R.id.roomText, roomList);
        roomListView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("chatrooms");

        userFullName = getIntent().getStringExtra("USER_FULL_NAME");
        if (userFullName == null || userFullName.isEmpty()) {
            Log.e(TAG, "User full name is null or empty");
            Toast.makeText(this, "Error: User information is missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fetchRooms();

        roomListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRoom = roomList.get(position);
            Intent intent = new Intent(StudyRoomListActivity.this, ChatActivity.class);
            intent.putExtra("ROOM_NAME", selectedRoom);
            intent.putExtra("USER_FULL_NAME", userFullName);
            startActivity(intent);
        });

        findViewById(R.id.addroom).setOnClickListener(v -> showAddRoomDialog());
    }

    private void showAddRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Room");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String roomName = input.getText().toString().trim();
            if (!roomName.isEmpty()) {
                addRoomToDatabase(roomName);
            } else {
                Toast.makeText(StudyRoomListActivity.this, "Room name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addRoomToDatabase(String roomName) {
        Log.d(TAG, "Attempting to add room: " + roomName);
        databaseReference.child(roomName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "Room already exists: " + roomName);
                    Toast.makeText(StudyRoomListActivity.this, "Room already exists", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> newRoom = new HashMap<>();
                    newRoom.put("createdBy", userFullName);

                    databaseReference.child(roomName).setValue(newRoom)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Room added successfully: " + roomName);
                                    Toast.makeText(StudyRoomListActivity.this, "Room added successfully", Toast.LENGTH_SHORT).show();
                                    fetchRooms(); // Refresh the room list
                                } else {
                                    Log.e(TAG, "Failed to add room: " + roomName, task.getException());
                                    Toast.makeText(StudyRoomListActivity.this, "Failed to add room: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase operation cancelled", error.toException());
                Toast.makeText(StudyRoomListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRooms() {
        Log.d(TAG, "Fetching rooms from Firebase");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomList.clear();
                for (DataSnapshot roomSnapshot : dataSnapshot.getChildren()) {
                    String roomName = roomSnapshot.getKey();
                    if (roomName != null) {
                        roomList.add(roomName);
                        Log.d(TAG, "Room fetched: " + roomName);
                    }
                }
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Room list updated. Total rooms: " + roomList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching rooms", databaseError.toException());
                Toast.makeText(StudyRoomListActivity.this, "Failed to fetch rooms: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}