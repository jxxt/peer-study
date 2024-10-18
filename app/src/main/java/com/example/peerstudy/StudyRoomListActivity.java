package com.example.peerstudy;

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
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roomList);
        roomListView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("chatrooms");
        fetchRooms();
    }

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
                // No error logs here
            }
        });
    }
}
